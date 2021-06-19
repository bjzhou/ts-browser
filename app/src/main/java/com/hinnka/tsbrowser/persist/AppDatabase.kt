package com.hinnka.tsbrowser.persist

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hinnka.tsbrowser.App
import zlc.season.rxdownload4.recorder.StatusConverter
import zlc.season.rxdownload4.recorder.TaskEntity

@Database(entities = [TabInfo::class, SearchHistory::class, History::class, Favorite::class, TaskEntity::class], version = 1, exportSchema = false)
@TypeConverters(StatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        val instance: AppDatabase by lazy {
            Room.databaseBuilder(App.instance, AppDatabase::class.java, "app-db-${App.processName}")
                .allowMainThreadQueries()
                .build()
        }
    }
}