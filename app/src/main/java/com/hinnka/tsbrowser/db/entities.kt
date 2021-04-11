package com.hinnka.tsbrowser.db

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TabInfo(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo var isActive: Boolean = false,
    @ColumnInfo var title: String = "",
    @ColumnInfo var iconPath: String? = "",
    @ColumnInfo var url: String = "",
    @ColumnInfo var thumbnailPath: String? = "",
)

fun TabInfo.delete() {
    AppDatabase.instance.tabDao().delete(this)
}

fun TabInfo.update() {
    try {
        AppDatabase.instance.tabDao().update(this)
    } catch (e: Exception) {
        Log.e("TSBrowser", "update error", e)
    }
}