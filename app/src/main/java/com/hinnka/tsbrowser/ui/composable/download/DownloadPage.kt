package com.hinnka.tsbrowser.ui.composable.download

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadNotificationCreator
import com.hinnka.tsbrowser.ext.longPress
import com.hinnka.tsbrowser.ui.base.TSAppBar
import io.reactivex.android.schedulers.AndroidSchedulers
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.manager.*
import zlc.season.rxdownload4.recorder.RoomRecorder
import zlc.season.rxdownload4.recorder.RxDownloadRecorder
import zlc.season.rxdownload4.recorder.TaskEntity

@Composable
fun DownloadPage() {
    val tasks = remember { mutableStateListOf<TaskEntity>() }
    val context = LocalContext.current
    Scaffold(topBar = {
        TSAppBar(title = stringResource(id = R.string.downloads), actions = {
            Box(modifier = Modifier.fillMaxHeight().clickable {
                RxDownloadRecorder.getAllTaskWithStatus(Completed())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it.isNullOrEmpty()) return@subscribe
                        AlertDialog.Builder(context).apply {
                            setTitle(R.string.clear)
                            setMessage(context.getString(R.string.clear_confirm, it.size))
                            setPositiveButton(R.string.delete) { _, _ ->
                                it.forEach { entity ->
                                    val manager = entity.task.manager(
                                        recorder = RoomRecorder(),
                                        notificationCreator = DownloadNotificationCreator()
                                    )
                                    manager.delete()
                                    tasks.removeAll { item -> item.id == entity.id }
                                }
                            }
                            setNegativeButton(android.R.string.cancel) { _, _ ->
                            }
                        }.show()
                    }
            }, contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.clear),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1,
                )
            }
        })
    }) {
        DisposableEffect(key1 = tasks) {
            val disposable = RxDownloadRecorder.getAllTask().subscribe { list ->
                println("TSBrowser $list")
                list?.let {
                    tasks.clear()
                    tasks.addAll(it.sortedBy { item ->
                        when (item.status) {
                            is Completed -> {
                                System.currentTimeMillis() - item.task.file().lastModified()
                            }
                            is Failed -> -1L
                            is Paused -> -2L
                            is Pending -> -3L
                            else -> -4L
                        }
                    })
                }
            }
            onDispose {
                disposable.dispose()
            }
        }

        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = stringResource(id = R.string.downloads_empty),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
        val state = rememberLazyListState()
        LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
            itemsIndexed(tasks) { index, entity ->

                val showPopup = remember { mutableStateOf(false) }

                Box(modifier = Modifier
                    .longPress {
                        showPopup.value = true
                    }
                    .height(100.dp)) {
                    val manager = entity.task.manager(
                        recorder = RoomRecorder(),
                        notificationCreator = DownloadNotificationCreator()
                    )

                    when (entity.status) {
                        is Completed -> CompletedItem(entity = entity)
                        is Failed -> ErrorItem(entity = entity)
                        else -> {
                            LaunchedEffect(key1 = entity) {
                                manager.subscribe {
                                    entity.status = it
                                    tasks[index] = entity
                                }
                            }

                            DownloadingItem(entity)
                        }
                    }
                    val clipboardManager = LocalClipboardManager.current
                    LongPressPopup(
                        showPopup = showPopup,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(entity.task.url))
                        },
                        onDelete = {
                            manager.delete()
                            tasks.removeAll { item -> item.id == entity.id }
                        },
                    )
                }
            }
        }
    }
}
