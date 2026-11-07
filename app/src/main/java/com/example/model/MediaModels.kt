package com.example.model

data class TrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: String = "",
    val albumArtUri: String? = null,
    val isFavorite: Boolean = false
)

data class VideoItem(
    val id: String,
    val title: String,
    val durationMs: Long,
    val resolution: String,
    val sizeMb: Double,
    val uri: String = "",
    val thumbnailUri: String? = null
) {
    val uriString: String get() = uri
}

data class PlaylistItem(
    val id: String,
    val name: String,
    val trackCount: Int,
    val durationText: String,
    val iconColorHex: String = "#00F0FF"
)

data class FolderItem(
    val name: String,
    val path: String,
    val audioCount: Int,
    val videoCount: Int
)

data class ArtistItem(
    val name: String,
    val trackCount: Int,
    val albumCount: Int
)

data class AlbumItem(
    val name: String,
    val artist: String,
    val trackCount: Int,
    val year: String = "2024"
)
