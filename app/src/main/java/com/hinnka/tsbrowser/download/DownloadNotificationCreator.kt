package com.hinnka.tsbrowser.download

import zlc.season.rxdownload4.notification.createNotificationBuilder
import zlc.season.rxdownload4.notification.createNotificationChannel
import zlc.season.rxdownload4.notification.isEnableNotification

import android.app.Notification
import android.content.Intent
import androidx.core.app.NotificationCompat.Builder
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.getPendingIntent
import com.hinnka.tsbrowser.ui.home.MainActivity
import zlc.season.claritypotion.ClarityPotion.Companion.clarityPotion
import zlc.season.rxdownload4.manager.*
import zlc.season.rxdownload4.notification.NotificationActionService.Companion.cancelAction
import zlc.season.rxdownload4.notification.NotificationActionService.Companion.startAction
import zlc.season.rxdownload4.notification.NotificationActionService.Companion.stopAction
import zlc.season.rxdownload4.task.Task
import zlc.season.rxdownload4.utils.log

class DownloadNotificationCreator : NotificationCreator {
    private val channelId = "Download"
    private val channelName = "TS Downloads"
    private val channelDesc = ""

    private lateinit var task: Task
    private val builderHelper by lazy { BuilderHelper(channelId, task) }

    override fun init(task: Task) {
        this.task = task

        if (!isEnableNotification()) {
            "Notification not enable".log()
        }

        createNotificationChannel(channelId, channelName, channelDesc)
    }

    override fun create(task: Task, status: Status): Notification? {
        return builderHelper.get(status)?.build()
    }

    class BuilderHelper(private val channelId: String, private val task: Task) {
        private val builderMap = mutableMapOf<Status, Builder>()

        private val pendingContent by lazy { clarityPotion.getString(R.string.notification_pending_text) }
        private val startedContent by lazy { clarityPotion.getString(R.string.notification_started_text) }
        private val pausedContent by lazy { clarityPotion.getString(R.string.notification_paused_text) }
        private val failedContent by lazy { clarityPotion.getString(R.string.notification_failed_text) }
        private val completedContent by lazy { clarityPotion.getString(R.string.notification_completed_text) }

        private val pendingActions by lazy { listOf(stopAction(task), cancelAction(task)) }
        private val startedActions by lazy { listOf(stopAction(task), cancelAction(task)) }
        private val downloadingActions by lazy { listOf(stopAction(task), cancelAction(task)) }
        private val pausedActions by lazy { listOf(startAction(task), cancelAction(task)) }
        private val failedActions by lazy { listOf(startAction(task), cancelAction(task)) }

        fun get(status: Status): Builder? {
            val builder = getBuilder(status)

            when (status) {
                is Downloading -> builder.setProgress(
                    status.progress.totalSize.toInt(),
                    status.progress.downloadSize.toInt(),
                    status.progress.isChunked
                )
                //Do not need notification
                is Normal -> return null
                is Deleted -> return null
                else -> {}
            }
            return builder
        }

        private fun getBuilder(status: Status): Builder {
            val builder = builderMap[status]

            if (builder == null) {
                val (content, actions, icon) = when (status) {
                    is Normal -> Triple("", emptyList(), 0)
                    is Pending -> Triple(pendingContent, pendingActions, R.drawable.ic_download)
                    is Started -> Triple(startedContent, startedActions, R.drawable.ic_download)
                    is Downloading -> Triple("", downloadingActions, R.drawable.ic_download)
                    is Paused -> Triple(pausedContent, pausedActions, R.drawable.ic_pause)
                    is Failed -> Triple(failedContent, failedActions, R.drawable.ic_pause)
                    is Completed -> Triple(completedContent, emptyList(), R.drawable.ic_completed)
                    is Deleted -> Triple("", emptyList(), 0)
                }

                val newBuilder = createNotificationBuilder(
                    channelId = channelId,
                    title = task.taskName,
                    content = content,
                    icon = icon,
                    intent = if (status is Completed) OpenReceiver.getPendingIntent(
                        App.instance, task.url
                    ) else getPendingIntent(Intent(App.instance, MainActivity::class.java).apply {
                        action = "navigate"
                        putExtra("route", "downloads")
                    }),
                    actions = actions
                )
                builderMap[status] = newBuilder

                return newBuilder
            } else {
                return builder
            }
        }
    }
}