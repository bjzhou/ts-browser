package com.hinnka.tsbrowser.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hinnka.tsbrowser.App

@Database(entities = [TabInfo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao

    companion object {
        val instance: AppDatabase by lazy {
            Room.databaseBuilder(App.instance, AppDatabase::class.java, "app-db-${App.getProcessName()}")
                .allowMainThreadQueries()
                .build()
        }
    }
}