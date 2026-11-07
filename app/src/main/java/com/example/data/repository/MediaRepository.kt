package com.example.data.repository

import com.example.data.local.MatrixDatabase
import com.example.data.local.entity.MediaItemEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistItemEntity
import com.example.data.media.LocalMediaScanner
import com.example.model.TrackItem
import com.example.model.VideoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaRepository(
    private val database: MatrixDatabase,
    private val scanner: LocalMediaScanner
) {
    val audioTracks: Flow<List<TrackItem>> = database.mediaDao().getAllAudio().map { entities ->
        entities.map { it.toTrackItem() }
    }

    val videos: Flow<List<VideoItem>> = database.mediaDao().getAllVideos().map { entities ->
        entities.map { it.toVideoItem() }
    }

    val favorites: Flow<List<TrackItem>> = database.mediaDao().getFavorites().map { entities ->
        entities.map { it.toTrackItem() }
    }

    val playbackHistory: Flow<List<TrackItem>> = database.historyDao().getRecentHistory().map { entities ->
        entities.map { it.toTrackItem() }
    }

    val playlists: Flow<List<com.example.model.PlaylistItem>> = database.playlistDao().getAllPlaylists().map { entities ->
        entities.map { entity ->
            com.example.model.PlaylistItem(
                id = entity.id,
                name = entity.name,
                trackCount = 0,
                durationText = "0m",
                iconColorHex = entity.iconColorHex
            )
        }
    }

    suspend fun createPlaylist(name: String, iconColorHex: String = "#00F0FF"): String {
        val id = "pl_${System.currentTimeMillis()}"
        database.playlistDao().insertPlaylist(
            PlaylistEntity(
                id = id,
                name = name,
                iconColorHex = iconColorHex
            )
        )
        return id
    }

    suspend fun deletePlaylist(playlistId: String) {
        database.playlistDao().deletePlaylistById(playlistId)
        database.playlistDao().clearPlaylistItems(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, mediaId: String) {
        database.playlistDao().addTrackToPlaylist(
            PlaylistItemEntity(
                playlistId = playlistId,
                mediaId = mediaId
            )
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, mediaId: String) {
        database.playlistDao().removeTrackFromPlaylist(playlistId, mediaId)
    }

    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackItem>> =
        database.playlistDao().getTracksForPlaylist(playlistId).map { entities ->
            entities.map { it.toTrackItem() }
        }

    fun searchAudio(query: String): Flow<List<TrackItem>> =
        database.mediaDao().searchAudio(query).map { entities ->
            entities.map { it.toTrackItem() }
        }

    fun getRecentlyAdded(limit: Int = 20): Flow<List<TrackItem>> =
        database.mediaDao().getRecentlyAddedAudio(limit).map { entities ->
            entities.map { it.toTrackItem() }
        }

    suspend fun updateLyrics(mediaId: String, lyrics: String) {
        database.mediaDao().updateLyrics(mediaId, lyrics)
    }

    suspend fun incrementPlayCount(mediaId: String) {
        database.mediaDao().incrementPlayCount(mediaId)
    }

    suspend fun scanLocalMedia() {
        val audio = scanner.scanAudioFiles()
        val videos = scanner.scanVideoFiles()
        if (audio.isNotEmpty() || videos.isNotEmpty()) {
            database.mediaDao().insertMediaItems(audio + videos)
        }
    }

    suspend fun toggleFavorite(mediaId: String, isFavorite: Boolean) {
        database.mediaDao().updateFavorite(mediaId, isFavorite)
    }

    suspend fun recordHistory(mediaId: String, positionMs: Long) {
        database.historyDao().recordHistory(
            PlaybackHistoryEntity(
                mediaId = mediaId,
                lastPlayedTimestamp = System.currentTimeMillis(),
                lastPositionMs = positionMs
            )
        )
    }

    suspend fun populateInitialDataIfEmpty(initialTracks: List<TrackItem>, initialVideos: List<VideoItem>) {
        val current = database.mediaDao().getMediaById(initialTracks.first().id)
        if (current == null) {
            val audioEntities = initialTracks.map {
                MediaItemEntity(
                    id = it.id,
                    title = it.title,
                    artist = it.artist,
                    album = it.album,
                    durationMs = it.durationMs,
                    uri = it.uri,
                    isVideo = false,
                    isFavorite = it.isFavorite
                )
            }
            val videoEntities = initialVideos.map {
                MediaItemEntity(
                    id = it.id,
                    title = it.title,
                    artist = "Video",
                    album = "Matrix Videos",
                    durationMs = it.durationMs,
                    uri = it.uri,
                    isVideo = true,
                    resolution = it.resolution,
                    sizeBytes = (it.sizeMb * 1024 * 1024).toLong()
                )
            }
            database.mediaDao().insertMediaItems(audioEntities + videoEntities)
        }
    }

    private fun MediaItemEntity.toTrackItem() = TrackItem(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        uri = uri,
        albumArtUri = albumArtUri,
        isFavorite = isFavorite
    )

    private fun MediaItemEntity.toVideoItem() = VideoItem(
        id = id,
        title = title,
        durationMs = durationMs,
        resolution = resolution ?: "1080p",
        sizeMb = if (sizeBytes > 0) sizeBytes.toDouble() / (1024 * 1024) else 150.0,
        uri = uri
    )
}
