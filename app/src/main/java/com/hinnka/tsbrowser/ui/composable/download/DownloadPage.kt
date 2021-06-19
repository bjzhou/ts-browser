package com.hinnka.tsbrowser.ui.composable.download

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.download.DownloadNotificationCreator
import com.hinnka.tsbrowser.download.TSRecorder
import com.hinnka.tsbrowser.ext.longPress
import com.hinnka.tsbrowser.ext.mainScope
import com.hinnka.tsbrowser.persist.AppDatabase
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.manager.*
import zlc.season.rxdownload4.recorder.RoomRecorder
import zlc.season.rxdownload4.recorder.RxDownloadRecorder
import zlc.season.rxdownload4.recorder.TaskEntity

@SuppressLint("CheckResult")
@Composable
fun DownloadPage() {
    val tasks = remember { mutableStateListOf<TaskEntity>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        DownloadHandler.showDownloadingBadge.value = false
    }

    Scaffold(topBar = {
        TSAppBar(title = stringResource(id = R.string.downloads), actions = {
            Box(modifier = Modifier
                .fillMaxHeight()
                .clickable {
                    scope.launch {
                        val it = AppDatabase.instance
                            .downloadDao()
                            .getAllWithStatus(Completed(), Failed())
                        if (it.isNullOrEmpty()) return@launch
                        AlertBottomSheet
                            .Builder(context)
                            .apply {
                                setTitle(R.string.clear)
                                setMessage(
                                    context.getString(
                                        R.string.clear_confirm,
                                        it.size
                                    )
                                )
                                setPositiveButton(R.string.delete) {
                                    it.forEach { entity ->
                                        val manager = entity.task.manager(
                                            recorder = RoomRecorder(),
                                            notificationCreator = DownloadNotificationCreator()
                                        )
                                        manager.delete()
                                        tasks.removeAll { item -> item.id == entity.id }
                                    }
                                }
                                setNegativeButton(android.R.string.cancel) {
                                }
                            }
                            .show()
                    }
                }, contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.clear),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1,
                )
            }
        })
    }) {
        LaunchedEffect(key1 = tasks) {
            scope.launch {
                val list = AppDatabase.instance.downloadDao().getAll()
                tasks.clear()
                tasks.addAll(list.sortedBy { item ->
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
                val popupOffset = remember { mutableStateOf(DpOffset.Zero) }
                val manager = entity.task.manager(
                    recorder = TSRecorder(),
                    notificationCreator = DownloadNotificationCreator()
                )
                val clipboardManager = LocalClipboardManager.current
                val density = LocalDensity.current

                Box(modifier = Modifier
                    .longPress {
                        showPopup.value = true
                        popupOffset.value = density.run { DpOffset(it.x.toDp(), it.y.toDp()) }
                    }
                    .height(100.dp)) {
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
                }

                DropdownMenu(
                    expanded = showPopup.value,
                    offset = popupOffset.value,
                    onDismissRequest = { showPopup.value = false },
                ) {
                    DropdownMenuItem(onClick = {
                        clipboardManager.setText(AnnotatedString(entity.task.url))
                        showPopup.value = false
                    }) {
                        Text(
                            text = stringResource(id = R.string.copy_link),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    DropdownMenuItem(onClick = {
                        manager.delete()
                        tasks.removeAll { item -> item.id == entity.id }
                        showPopup.value = false
                    }) {
                        Text(
                            text = stringResource(id = R.string.delete),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
