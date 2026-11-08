package com.example.media.sync

data class SyncFolderInfo(
    val name: String,
    val path: String,
    val audioCount: Int,
    val videoCount: Int,
    val totalSizeBytes: Long,
    val isCustomSafFolder: Boolean = false
) {
    val totalMediaCount: Int get() = audioCount + videoCount
    val totalSizeMbText: String
        get() {
            val mb = totalSizeBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1024) {
                String.format("%.2f GB", mb / 1024)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}

enum class MediaSyncStatus(val label: String) {
    IDLE("Ready"),
    SCANNING("Synchronizing..."),
    COMPLETED("Synchronized"),
    ERROR("Sync Error")
}

data class AutoSyncReport(
    val status: MediaSyncStatus = MediaSyncStatus.IDLE,
    val totalAudioCount: Int = 0,
    val totalVideoCount: Int = 0,
    val totalMediaSizeBytes: Long = 0L,
    val lastSyncTimestamp: Long = 0L,
    val newlyDiscoveredCount: Int = 0,
    val currentScanningFolder: String = "",
    val isAutoSyncActive: Boolean = true,
    val foldersSummary: List<SyncFolderInfo> = emptyList(),
    val statusMessage: String = "Auto-sync ready to grab all local storage media"
) {
    val totalMediaCount: Int get() = totalAudioCount + totalVideoCount
    val totalSizeText: String
        get() {
            val mb = totalMediaSizeBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1024) {
                String.format("%.2f GB", mb / 1024)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}
