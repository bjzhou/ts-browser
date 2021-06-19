package com.hinnka.tsbrowser.download

import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.persist.AppDatabase
import kotlinx.coroutines.launch
import zlc.season.rxdownload4.manager.Normal
import zlc.season.rxdownload4.manager.Status
import zlc.season.rxdownload4.manager.TaskRecorder
import zlc.season.rxdownload4.recorder.TaskEntity
import zlc.season.rxdownload4.task.Task

class TSRecorder : TaskRecorder {
    override fun delete(task: Task) {
        ioScope.launch {
            AppDatabase.instance.downloadDao().delete(task.map())
        }
    }

    override fun insert(task: Task) {
        ioScope.launch {
            AppDatabase.instance.downloadDao().insert(task.map())
        }
    }

    override fun update(task: Task, status: Status) {
        ioScope.launch {
            AppDatabase.instance.downloadDao().update(task.map(status))
        }
    }
}

internal fun Task.map(status: Status = Normal()): TaskEntity {
    return TaskEntity(
        id = hashCode(),
        task = this,
        status = status,
        progress = status.progress
    )
}