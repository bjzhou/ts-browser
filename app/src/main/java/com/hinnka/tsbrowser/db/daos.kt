package com.hinnka.tsbrowser.db

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface TabDao {
    @Query("SELECT * FROM tabinfo")
    fun getAll(): List<TabInfo>

    @Insert
    fun insert(tab: TabInfo): Long

    @Update(onConflict = REPLACE)
    fun update(vararg tab: TabInfo)

    @Delete
    fun delete(tab: TabInfo)
}