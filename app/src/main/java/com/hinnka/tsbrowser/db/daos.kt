package com.hinnka.tsbrowser.db

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface TabDao {
    @Query("SELECT * FROM tabinfo")
    suspend fun getAll(): List<TabInfo>

    @Insert
    suspend fun insert(tab: TabInfo): Long

    @Update(onConflict = REPLACE)
    suspend fun update(vararg tab: TabInfo)

    @Delete
    suspend fun delete(tab: TabInfo)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM searchhistory ORDER BY updatedAt")
    suspend fun getAll(): List<SearchHistory>

    @Query("SELECT * FROM searchhistory WHERE `query` = :query")
    suspend fun getByName(query: String): SearchHistory?

    @Insert(onConflict = REPLACE)
    suspend fun insert(query: SearchHistory)

    @Update
    suspend fun update(query: SearchHistory)

    @Delete
    suspend fun delete(vararg query: SearchHistory)
}