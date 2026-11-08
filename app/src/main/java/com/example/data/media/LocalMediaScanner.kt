package com.example.data.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.example.data.local.entity.MediaItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalMediaScanner(private val context: Context) {

    suspend fun scanAudioFiles(): List<MediaItemEntity> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<MediaItemEntity>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection.add(MediaStore.Audio.Media.RELATIVE_PATH)
        }
        @Suppress("DEPRECATION")
        projection.add(MediaStore.Audio.Media.DATA)

        // Capture all valid audio files >= 1 second and size > 0
        val selection = "${MediaStore.Audio.Media.SIZE} > 0 AND ${MediaStore.Audio.Media.DURATION} >= 1000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection.toTypedArray(),
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val trackColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val relativePathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                } else -1
                @Suppress("DEPRECATION")
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/mpeg"
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val year = if (yearColumn >= 0) cursor.getString(yearColumn) else null
                    val track = if (trackColumn >= 0) cursor.getInt(trackColumn) else 0

                    val folder = extractFolderName(cursor, relativePathColumn, dataColumn, defaultFolder = "Music")

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    audioList.add(
                        MediaItemEntity(
                            id = "audio_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            durationMs = duration,
                            uri = contentUri.toString(),
                            albumArtUri = albumArtUri,
                            mimeType = mimeType,
                            isVideo = false,
                            sizeBytes = size,
                            dateAdded = dateAdded * 1000L,
                            year = year,
                            trackNumber = track,
                            folderPath = folder
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        audioList
    }

    suspend fun scanVideoFiles(): List<MediaItemEntity> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<MediaItemEntity>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_ADDED
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection.add(MediaStore.Video.Media.RELATIVE_PATH)
        }
        @Suppress("DEPRECATION")
        projection.add(MediaStore.Video.Media.DATA)

        val selection = "${MediaStore.Video.Media.SIZE} > 0"
        val sortOrder = "${MediaStore.Video.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection.toTypedArray(),
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val relativePathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                } else -1
                @Suppress("DEPRECATION")
                val dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Video"
                    val duration = cursor.getLong(durationColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "video/mp4"
                    val size = cursor.getLong(sizeColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val folder = extractFolderName(cursor, relativePathColumn, dataColumn, defaultFolder = "Videos")

                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val resolution = if (width > 0 && height > 0) "${width}x${height}" else "1080p"

                    videoList.add(
                        MediaItemEntity(
                            id = "video_$id",
                            title = title,
                            artist = "Video",
                            album = folder,
                            durationMs = duration,
                            uri = contentUri.toString(),
                            mimeType = mimeType,
                            isVideo = true,
                            resolution = resolution,
                            sizeBytes = size,
                            dateAdded = dateAdded * 1000L,
                            folderPath = folder
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videoList
    }

    /**
     * Traverses custom Storage Access Framework (SAF) tree directory
     * and extracts any audio or video files into MediaItemEntity.
     */
    suspend fun scanCustomSafDirectory(treeUri: Uri): Pair<List<MediaItemEntity>, List<MediaItemEntity>> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<MediaItemEntity>()
        val videoList = mutableListOf<MediaItemEntity>()

        try {
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocId)

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            )

            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val modCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val docId = cursor.getString(idCol)
                    val name = cursor.getString(nameCol) ?: "Media File"
                    val mime = cursor.getString(mimeCol) ?: ""
                    val size = cursor.getLong(sizeCol)
                    val modified = cursor.getLong(modCol)

                    val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    val lower = name.lowercase()

                    val isAudio = mime.startsWith("audio/") || lower.endsWith(".mp3") || lower.endsWith(".flac") ||
                            lower.endsWith(".wav") || lower.endsWith(".m4a") || lower.endsWith(".aac") || lower.endsWith(".ogg")
                    val isVideo = mime.startsWith("video/") || lower.endsWith(".mp4") || lower.endsWith(".mkv") ||
                            lower.endsWith(".webm") || lower.endsWith(".avi") || lower.endsWith(".mov")

                    if (isAudio) {
                        val cleanTitle = name.substringBeforeLast(".")
                        audioList.add(
                            MediaItemEntity(
                                id = "saf_audio_${docId.hashCode()}",
                                title = cleanTitle,
                                artist = "External Storage",
                                album = "SAF Media",
                                durationMs = 0L,
                                uri = docUri.toString(),
                                mimeType = mime.ifEmpty { "audio/mpeg" },
                                isVideo = false,
                                sizeBytes = size,
                                dateAdded = modified,
                                folderPath = "Custom Storage"
                            )
                        )
                    } else if (isVideo) {
                        val cleanTitle = name.substringBeforeLast(".")
                        videoList.add(
                            MediaItemEntity(
                                id = "saf_video_${docId.hashCode()}",
                                title = cleanTitle,
                                artist = "Video",
                                album = "Custom Storage",
                                durationMs = 0L,
                                uri = docUri.toString(),
                                mimeType = mime.ifEmpty { "video/mp4" },
                                isVideo = true,
                                resolution = "HD",
                                sizeBytes = size,
                                dateAdded = modified,
                                folderPath = "Custom Storage"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Pair(audioList, videoList)
    }

    private fun extractFolderName(cursor: Cursor, relPathCol: Int, dataCol: Int, defaultFolder: String): String {
        if (relPathCol >= 0) {
            val rel = cursor.getString(relPathCol)
            if (!rel.isNullOrBlank()) {
                val cleaned = rel.trim('/').replace('/', ' ')
                val topFolder = rel.trim('/').split('/').firstOrNull()?.trim()
                if (!topFolder.isNullOrBlank()) return topFolder
            }
        }
        if (dataCol >= 0) {
            val data = cursor.getString(dataCol)
            if (!data.isNullOrBlank()) {
                val parent = File(data).parentFile?.name
                if (!parent.isNullOrBlank() && parent != "0" && parent != "emulated") {
                    return parent
                }
            }
        }
        return defaultFolder
    }
}
