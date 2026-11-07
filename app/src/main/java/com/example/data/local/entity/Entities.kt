package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: String,
    val albumArtUri: String? = null,
    val mimeType: String = "audio/mpeg",
    val isVideo: Boolean = false,
    val resolution: String? = null,
    val sizeBytes: Long = 0L,
    val folderPath: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val year: String? = null,
    val trackNumber: Int = 0,
    val bitrate: Int = 320,
    val sampleRate: Int = 44100,
    val playCount: Int = 0,
    val lyrics: String? = null
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val iconColorHex: String = "#00F0FF",
    val isSmartPlaylist: Boolean = false
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlistId", "mediaId"])
data class PlaylistItemEntity(
    val playlistId: String,
    val mediaId: String,
    val sortOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey val mediaId: String,
    val lastPlayedTimestamp: Long = System.currentTimeMillis(),
    val lastPositionMs: Long = 0L,
    val playCount: Int = 1
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mediaId: String,
    val positionMs: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "equalizer_presets")
data class EqualizerPresetEntity(
    @PrimaryKey val name: String,
    val bandGainsJson: String, // Comma-separated or JSON float array
    val bassBoost: Float = 0f,
    val isCustom: Boolean = false
)

@Entity(tableName = "media_sources")
data class MediaSourceEntity(
    @PrimaryKey val pathOrUrl: String,
    val displayName: String,
    val isLocalDirectory: Boolean = true,
    val isExcluded: Boolean = false,
    val isGitHubRaw: Boolean = false,
    val lastScanned: Long = System.currentTimeMillis()
)
