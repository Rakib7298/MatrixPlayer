package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.EqualizerPresetEntity
import com.example.data.local.entity.MediaItemEntity
import com.example.data.local.entity.MediaSourceEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items WHERE isVideo = 0 ORDER BY title ASC")
    fun getAllAudio(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isVideo = 1 ORDER BY title ASC")
    fun getAllVideos(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavorites(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: String): MediaItemEntity?

    @Query("SELECT DISTINCT album FROM media_items WHERE isVideo = 0 AND album != '' ORDER BY album ASC")
    fun getAllAlbums(): Flow<List<String>>

    @Query("SELECT DISTINCT artist FROM media_items WHERE isVideo = 0 AND artist != '' ORDER BY artist ASC")
    fun getAllArtists(): Flow<List<String>>

    @Query("SELECT * FROM media_items WHERE album = :album AND isVideo = 0 ORDER BY title ASC")
    fun getTracksByAlbum(album: String): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE artist = :artist AND isVideo = 0 ORDER BY title ASC")
    fun getTracksByArtist(artist: String): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isVideo = 0 AND (title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%') ORDER BY title ASC")
    fun searchAudio(query: String): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isVideo = 0 ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAddedAudio(limit: Int): Flow<List<MediaItemEntity>>

    @Query("UPDATE media_items SET playCount = playCount + 1, dateAdded = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE media_items SET lyrics = :lyrics WHERE id = :id")
    suspend fun updateLyrics(id: String, lyrics: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(items: List<MediaItemEntity>)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM media_items WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM media_items")
    suspend fun clearAll()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: String)

    @Query("SELECT m.* FROM media_items m INNER JOIN playlist_items p ON m.id = p.mediaId WHERE p.playlistId = :playlistId ORDER BY p.sortOrder ASC")
    fun getTracksForPlaylist(playlistId: String): Flow<List<MediaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToPlaylist(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND mediaId = :mediaId")
    suspend fun removeTrackFromPlaylist(playlistId: String, mediaId: String)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylistItems(playlistId: String)
}

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT m.* FROM media_items m INNER JOIN playback_history h ON m.id = h.mediaId ORDER BY h.lastPlayedTimestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<MediaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordHistory(history: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE mediaId = :mediaId ORDER BY positionMs ASC")
    fun getBookmarksForMedia(mediaId: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)
}

@Dao
interface EqualizerPresetDao {
    @Query("SELECT * FROM equalizer_presets ORDER BY isCustom DESC, name ASC")
    fun getAllPresets(): Flow<List<EqualizerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPresetEntity)
}

@Dao
interface MediaSourceDao {
    @Query("SELECT * FROM media_sources WHERE isExcluded = 0")
    fun getActiveSources(): Flow<List<MediaSourceEntity>>

    @Query("SELECT * FROM media_sources WHERE isExcluded = 1")
    fun getExcludedSources(): Flow<List<MediaSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: MediaSourceEntity)

    @Query("UPDATE media_sources SET isExcluded = :isExcluded WHERE pathOrUrl = :pathOrUrl")
    suspend fun setExcluded(pathOrUrl: String, isExcluded: Boolean)

    @Query("DELETE FROM media_sources WHERE pathOrUrl = :pathOrUrl")
    suspend fun deleteSource(pathOrUrl: String)
}
