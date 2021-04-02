package com.hinnka.tsbrowser.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.get
import com.hinnka.tsbrowser.ext.getPendingIntent
import com.hinnka.tsbrowser.ext.notificationManager
import com.hinnka.tsbrowser.ui.home.MainActivity
import zlc.season.rxdownload4.file
import java.util.concurrent.atomic.AtomicInteger

object DownloadNotification {
    private val notifyId = AtomicInteger(100)
    private val notificationMap = mutableMapOf<String, Pair<Int, NotificationCompat.Builder>>()

    val notificationManager = App.instance.notificationManager
    const val channel = "download"

    private fun createNotification(fileName: String): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channel, App.instance[R.string.download_channel], NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager?.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(App.instance, channel).apply {
            setContentTitle(fileName)
            setProgress(100, 0, false)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setOngoing(true)
        }
    }

    fun notify(url: String, fileName: String, progress: Int) {
        val notificationPair = notificationMap[url] ?: run {
            val pair = notifyId.getAndIncrement() to createNotification(fileName)
            notificationMap[url] = pair
            pair
        }
        notificationManager?.notify(notificationPair.first, notificationPair.second.apply {
            setProgress(100, progress, false)
            setContentIntent(getPendingIntent(MainActivity::class.java))
        }.build())
    }

    fun notifyComplete(url: String) {
        val notificationPair = notificationMap[url] ?: run {
            val pair = notifyId.getAndIncrement() to createNotification(url.file().name)
            notificationMap[url] = pair
            pair
        }
        notificationManager?.cancel(notificationPair.first)
        notificationManager?.notify(notificationPair.first, notificationPair.second.apply {
            setOngoing(false)
            setProgress(0, 0, false)
            setContentText(App.instance[R.string.download_complete])
            setContentIntent(OpenReceiver.getPendingIntent(App.instance, url))
            setAutoCancel(true)
        }.build())
        notificationMap.remove(url)
    }
}