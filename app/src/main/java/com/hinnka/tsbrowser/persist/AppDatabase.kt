package com.hinnka.tsbrowser.persist

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hinnka.tsbrowser.App

@Database(entities = [TabInfo::class, SearchHistory::class, History::class, Favorite::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        val instance: AppDatabase by lazy {
            Room.databaseBuilder(App.instance, AppDatabase::class.java, "app-db-${App.getProcessName()}")
                .build()
        }
    }
}