package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.EqualizerPresetDao
import com.example.data.local.dao.MediaDao
import com.example.data.local.dao.MediaSourceDao
import com.example.data.local.dao.PlaybackHistoryDao
import com.example.data.local.dao.PlaylistDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.EqualizerPresetEntity
import com.example.data.local.entity.MediaItemEntity
import com.example.data.local.entity.MediaSourceEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistItemEntity

@Database(
    entities = [
        MediaItemEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        PlaybackHistoryEntity::class,
        BookmarkEntity::class,
        EqualizerPresetEntity::class,
        MediaSourceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MatrixDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): PlaybackHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun equalizerPresetDao(): EqualizerPresetDao
    abstract fun mediaSourceDao(): MediaSourceDao

    companion object {
        @Volatile
        private var INSTANCE: MatrixDatabase? = null

        fun getInstance(context: Context): MatrixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MatrixDatabase::class.java,
                    "matrix_player.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
