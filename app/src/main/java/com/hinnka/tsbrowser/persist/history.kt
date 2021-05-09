package com.hinnka.tsbrowser.persist

import androidx.paging.PagingSource
import androidx.room.*

@Entity
data class History(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo var url: String,
    @ColumnInfo var title: String,
    @ColumnInfo var date: Long,
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY date DESC")
    fun getAll(): PagingSource<Int, History>

    @Query("SELECT * FROM history WHERE url LIKE :query OR title LIKE :query ORDER BY date DESC")
    fun search(query: String): PagingSource<Int, History>

    @Query("SELECT * FROM history ORDER BY date DESC LIMIT 1")
    suspend fun last(): History?

    @Insert
    suspend fun insert(history: History): Long

    @Delete
    suspend fun delete(vararg query: History)

    @Query("DELETE FROM history")
    suspend fun clear()
}