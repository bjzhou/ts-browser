package com.hinnka.tsbrowser.persist

import androidx.room.*
import zlc.season.rxdownload4.manager.Status
import zlc.season.rxdownload4.recorder.TaskEntity

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(taskEntity: TaskEntity): Long

    @Update
    suspend fun update(taskEntity: TaskEntity): Int

    @Update
    suspend fun update(list: List<TaskEntity>): Int

    @Delete
    suspend fun delete(taskEntity: TaskEntity): Int

    @Query("SELECT * FROM task_record")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM task_record WHERE status IN(:status)")
    suspend fun getAllWithStatus(vararg status: Status): List<TaskEntity>

    @Query("SELECT * FROM task_record LIMIT :size OFFSET :start")
    suspend fun page(start: Int, size: Int): List<TaskEntity>

    @Query("SELECT * FROM task_record WHERE status IN(:status) LIMIT :size OFFSET :start")
    suspend fun pageWithStatus(start: Int, size: Int, vararg status: Status): List<TaskEntity>

    @Query("SELECT * FROM task_record WHERE id = :id")
    suspend fun get(id: Int): TaskEntity

    @Query("SELECT * FROM task_record WHERE id IN(:id)")
    suspend fun get(vararg id: Int): List<TaskEntity>

    @Query("UPDATE task_record SET extraInfo = :extraInfo WHERE id = :id")
    suspend fun update(id: Int, extraInfo: String): Int
}