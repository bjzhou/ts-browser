package com.hinnka.tsbrowser.persist

import androidx.room.*
import com.hinnka.tsbrowser.ext.ioScope
import kotlinx.coroutines.launch

@Entity
data class TabInfo(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo var isActive: Boolean = false,
    @ColumnInfo var title: String = "",
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

@Dao
interface TabDao {
    @Query("SELECT * FROM tabinfo")
    suspend fun getAll(): List<TabInfo>

    @Insert
    suspend fun insert(tab: TabInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(vararg tab: TabInfo)

    @Delete
    suspend fun delete(tab: TabInfo)
}