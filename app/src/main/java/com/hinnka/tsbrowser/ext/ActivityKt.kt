package com.hinnka.tsbrowser.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import com.hinnka.tsbrowser.App

@SuppressLint("UnspecifiedImmutableFlag")
fun Activity.pendingIntent(requestCode: Int = 0): PendingIntent {
    return PendingIntent.getActivity(this, requestCode, Intent(this, javaClass), PendingIntent.FLAG_UPDATE_CURRENT)
}

@SuppressLint("UnspecifiedImmutableFlag")
fun getPendingIntent(activityClass: Class<out Activity>, requestCode: Int = 0): PendingIntent {
    return PendingIntent.getActivity(App.instance, requestCode, Intent(App.instance, activityClass), PendingIntent.FLAG_UPDATE_CURRENT)
}

@SuppressLint("UnspecifiedImmutableFlag")
fun getPendingIntent(intent: Intent, requestCode: Int = 0): PendingIntent {
    return PendingIntent.getActivity(App.instance, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}