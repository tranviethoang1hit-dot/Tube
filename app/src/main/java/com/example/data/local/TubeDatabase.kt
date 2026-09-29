package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DownloadedVideoEntity::class,
        WatchHistoryEntity::class,
        SavedPlaylistEntity::class,
        LikedVideoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TubeDatabase : RoomDatabase() {
    abstract fun tubeDao(): TubeDao

    companion object {
        @Volatile
        private var INSTANCE: TubeDatabase? = null

        fun getDatabase(context: Context): TubeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TubeDatabase::class.java,
                    "tube_premium_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
