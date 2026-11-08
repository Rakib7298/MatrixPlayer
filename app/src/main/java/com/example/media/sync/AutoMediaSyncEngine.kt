package com.example.media.sync

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.example.data.local.MatrixDatabase
import com.example.data.local.entity.MediaItemEntity
import com.example.data.media.LocalMediaScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class AutoMediaSyncEngine(
    private val context: Context,
    private val database: MatrixDatabase,
    private val scanner: LocalMediaScanner
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _syncReport = MutableStateFlow(AutoSyncReport())
    val syncReport: StateFlow<AutoSyncReport> = _syncReport.asStateFlow()

    private val customSafUris = mutableListOf<Uri>()
    private val changeSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var isObserverRegistered = false
    private val isSyncInProgress = AtomicBoolean(false)
    private var debounceJob: Job? = null

    private val mediaContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            if (_syncReport.value.isAutoSyncActive) {
                scheduleDebouncedSync()
            }
        }
    }

    init {
        startAutoSyncObserver()
        // Initial automatic sync on engine initialization
        scope.launch {
            delay(500)
            syncAllMedia(isManual = false)
        }
    }

    fun startAutoSyncObserver() {
        if (!isObserverRegistered) {
            try {
                context.contentResolver.registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    true,
                    mediaContentObserver
                )
                context.contentResolver.registerContentObserver(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    true,
                    mediaContentObserver
                )
                isObserverRegistered = true
                _syncReport.value = _syncReport.value.copy(isAutoSyncActive = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAutoSyncObserver() {
        if (isObserverRegistered) {
            try {
                context.contentResolver.unregisterContentObserver(mediaContentObserver)
                isObserverRegistered = false
                _syncReport.value = _syncReport.value.copy(isAutoSyncActive = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        if (enabled) {
            startAutoSyncObserver()
        } else {
            stopAutoSyncObserver()
        }
    }

    private fun scheduleDebouncedSync() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(1500L) // Debounce rapid media writes
            syncAllMedia(isManual = false)
        }
    }

    fun triggerSyncNow() {
        scope.launch {
            syncAllMedia(isManual = true)
        }
    }

    fun addCustomFolder(uri: Uri) {
        if (!customSafUris.contains(uri)) {
            customSafUris.add(uri)
            try {
                val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            triggerSyncNow()
        }
    }

    fun removeCustomFolder(uri: Uri) {
        customSafUris.remove(uri)
        triggerSyncNow()
    }

    suspend fun syncAllMedia(isManual: Boolean = false): AutoSyncReport = withContext(Dispatchers.IO) {
        if (isSyncInProgress.getAndSet(true)) {
            return@withContext _syncReport.value
        }

        _syncReport.value = _syncReport.value.copy(
            status = MediaSyncStatus.SCANNING,
            statusMessage = "Scanning device storage for music and videos..."
        )

        try {
            // 1. Scan MediaStore Audio
            val audioEntities = scanner.scanAudioFiles()

            // 2. Scan MediaStore Video
            val videoEntities = scanner.scanVideoFiles()

            // 3. Scan Custom SAF folders if any registered
            val safAudios = mutableListOf<MediaItemEntity>()
            val safVideos = mutableListOf<MediaItemEntity>()
            for (safUri in customSafUris) {
                val (audios, vids) = scanner.scanCustomSafDirectory(safUri)
                safAudios.addAll(audios)
                safVideos.addAll(vids)
            }

            val allAudios = audioEntities + safAudios
            val allVideos = videoEntities + safVideos
            val allMedia = allAudios + allVideos

            // 4. Save to Room database
            if (allMedia.isNotEmpty()) {
                database.mediaDao().insertMediaItems(allMedia)
            }

            // 5. Build Folder Summaries
            val foldersMap = mutableMapOf<String, MutableList<MediaItemEntity>>()
            for (item in allMedia) {
                val folder = if (!item.folderPath.isNullOrBlank()) item.folderPath else if (item.isVideo) "Videos" else "Music"
                foldersMap.getOrPut(folder) { mutableListOf() }.add(item)
            }

            val foldersSummary = foldersMap.map { (folderName, items) ->
                val audiosInFolder = items.count { !it.isVideo }
                val videosInFolder = items.count { it.isVideo }
                val sizeBytes = items.sumOf { it.sizeBytes }
                SyncFolderInfo(
                    name = folderName,
                    path = "/storage/emulated/0/$folderName",
                    audioCount = audiosInFolder,
                    videoCount = videosInFolder,
                    totalSizeBytes = sizeBytes,
                    isCustomSafFolder = folderName == "Custom Storage"
                )
            }.sortedByDescending { it.totalMediaCount }

            val totalSize = allMedia.sumOf { it.sizeBytes }
            val now = System.currentTimeMillis()

            val completedReport = AutoSyncReport(
                status = MediaSyncStatus.COMPLETED,
                totalAudioCount = allAudios.size,
                totalVideoCount = allVideos.size,
                totalMediaSizeBytes = totalSize,
                lastSyncTimestamp = now,
                newlyDiscoveredCount = allMedia.size,
                isAutoSyncActive = isObserverRegistered,
                foldersSummary = foldersSummary,
                statusMessage = if (isManual) {
                    "Full synchronization completed! Indexed ${allAudios.size} audio and ${allVideos.size} video files across ${foldersSummary.size} folders."
                } else {
                    "Auto-sync completed. Found ${allAudios.size} audio and ${allVideos.size} video files."
                }
            )

            _syncReport.value = completedReport
            completedReport
        } catch (e: Exception) {
            e.printStackTrace()
            val errorReport = _syncReport.value.copy(
                status = MediaSyncStatus.ERROR,
                statusMessage = "Sync failed: ${e.message}"
            )
            _syncReport.value = errorReport
            errorReport
        } finally {
            isSyncInProgress.set(false)
        }
    }

    fun release() {
        stopAutoSyncObserver()
    }
}
