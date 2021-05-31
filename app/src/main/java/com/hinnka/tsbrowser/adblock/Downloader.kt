package com.hinnka.tsbrowser.adblock

import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.logD
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import zlc.season.rxdownload4.download
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.task.Task
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object Downloader {

    val hostUrl = "https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt"

    suspend fun getHost(): File {
        val task = Task(hostUrl, saveName = "hosts", savePath = App.instance.filesDir.path)
        val file = task.file()
        return if (file.exists()) {
            if ((System.currentTimeMillis() - file.lastModified() > TimeUnit.DAYS.toMillis(7))) {
                ioScope.launch {
                    download(task)
                }
            }
            logD("load local hosts")
            file
        } else {
            download(task)
        }
    }

    suspend fun download(task: Task): File {
        logD("download hosts")
        return suspendCancellableCoroutine { continuation ->
            task.download().doOnComplete {
                continuation.resume(task.file())
            }.subscribe()
        }
    }
}