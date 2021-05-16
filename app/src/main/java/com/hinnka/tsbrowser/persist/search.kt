package com.hinnka.tsbrowser.persist

import android.graphics.Bitmap
import androidx.room.*

@Entity
data class SearchHistory(
    @PrimaryKey var query: String,
    @ColumnInfo var updatedAt: Long,
    @ColumnInfo var title: String? = null,
    @ColumnInfo var url: String? = null
) {
    @Ignore
    var iconBitmap: Bitmap? = null
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM searchhistory ORDER BY updatedAt")
    suspend fun getAll(): List<SearchHistory>

    @Query("SELECT * FROM searchhistory WHERE `query` = :query")
    suspend fun getByName(query: String): SearchHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(query: SearchHistory)

    @Update
    suspend fun update(query: SearchHistory)

    @Delete
    suspend fun delete(vararg query: SearchHistory)

    @Query("DELETE FROM SearchHistory")
    suspend fun clear()
}