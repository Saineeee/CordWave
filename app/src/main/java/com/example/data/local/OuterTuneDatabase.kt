package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        HistoryEntity::class,
        DownloadEntity::class,
        QueueStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OuterTuneDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun queueDao(): QueueDao

    companion object {
        @Volatile
        private var INSTANCE: OuterTuneDatabase? = null

        fun getInstance(context: Context): OuterTuneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OuterTuneDatabase::class.java,
                    "outer_tune.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
