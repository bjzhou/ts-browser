package com.hinnka.tsbrowser.persist

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
    suspend fun getAll(): List<History>

    @Query("SELECT * FROM history WHERE url LIKE :query OR title LIKE :query")
    suspend fun search(query: String): History?

    @Insert
    suspend fun insert(history: History): Long

    @Delete
    suspend fun delete(vararg query: History)

    @Query("DELETE FROM history")
    suspend fun clear()
}