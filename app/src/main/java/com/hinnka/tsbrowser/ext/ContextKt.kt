package com.hinnka.tsbrowser.ext

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.getSystemService

operator fun Context.get(@StringRes id: Int): String = getString(id)

val Context.notificationManager: NotificationManager?
    get() = getSystemService()

val Context.downloadManager: DownloadManager?
    get() = getSystemService()