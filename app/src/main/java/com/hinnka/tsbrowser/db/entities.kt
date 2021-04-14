package com.hinnka.tsbrowser.db

import android.graphics.Bitmap
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hinnka.tsbrowser.ext.ioScope
import kotlinx.coroutines.launch

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
    ioScope.launch {
        AppDatabase.instance.tabDao().delete(this@delete)
    }
}

fun TabInfo.update() {
    ioScope.launch {
        AppDatabase.instance.tabDao().update(this@update)
    }
}

@Entity
data class SearchHistory(
    @PrimaryKey var query: String,
    @ColumnInfo var updatedAt: Long,
    @ColumnInfo var title: String? = null,
    @ColumnInfo var icon: String? = null
) {
    @Ignore
    var iconBitmap: Bitmap? = null
}