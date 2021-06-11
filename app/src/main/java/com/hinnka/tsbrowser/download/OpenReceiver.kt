package com.hinnka.tsbrowser.download

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.hinnka.tsbrowser.ext.mimeType
import zlc.season.rxdownload4.file
import java.io.File

class OpenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != action) return
        val url = intent.getStringExtra("url") ?: return
        val file = url.file()
        file.open(context)
        DownloadHandler.showDownloadingBadge.value = false
    }

    companion object {

        private val receiver = OpenReceiver()
        private const val action = "com.hinnka.action.OPEN_FILE"

        fun register(context: Context) {
            val filter = IntentFilter(action)
            try {
                context.registerReceiver(receiver, filter)
            } catch (e: Exception) {
            }
        }

        @SuppressLint("UnspecifiedImmutableFlag")
        fun getPendingIntent(context: Context, url: String): PendingIntent {
            val intent = Intent(action).apply {
                putExtra("url", url)
                setPackage(context.packageName)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}

fun File.open(context: Context) {
    val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", this)
    val openIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, this@open.mimeType)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(openIntent)
    } catch (e: Exception) {
    }
}