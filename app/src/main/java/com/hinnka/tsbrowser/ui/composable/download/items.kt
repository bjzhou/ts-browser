package com.hinnka.tsbrowser.ui.composable.download

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadNotificationCreator
import com.hinnka.tsbrowser.download.open
import com.hinnka.tsbrowser.ext.formatByte
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.manager.*
import zlc.season.rxdownload4.recorder.RoomRecorder
import zlc.season.rxdownload4.recorder.TaskEntity
import zlc.season.rxdownload4.utils.formatSize
import java.text.DateFormat
import java.util.*


@Composable
fun DownloadingItem(entity: TaskEntity) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFEAB2A5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Icon",
                tint = MaterialTheme.colors.primary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = entity.task.taskName,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
            )
            Text(
                text = entity.status.progress.downloadSize.formatByte() + " / " + entity.status.progress.totalSizeStr(),
                fontSize = 12.sp,
            )
            LinearProgressIndicator(
                progress = entity.status.progress.percent().toFloat() / 100f,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colors.primary
            )
        }
        Button(
            onClick = {
                val manager = entity.task.manager(
                    recorder = RoomRecorder(),
                    notificationCreator = DownloadNotificationCreator()
                )
                when (entity.status) {
                    is Pending -> manager.stop()
                    is Paused -> manager.start()
                    else -> manager.stop()
                }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFE1FFFA),
                contentColor = MaterialTheme.colors.primary
            ),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(
                    id = when (entity.status) {
                        is Pending -> R.string.pending
                        is Paused -> R.string.resume
                        else -> R.string.pause
                    }
                )
            )
        }
    }
}


@Composable
fun CompletedItem(entity: TaskEntity) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFEAB2A5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FileDownloadDone,
                contentDescription = "Icon",
                tint = MaterialTheme.colors.primary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = entity.task.saveName,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
            )
            val lastDate = DateFormat.getDateInstance().format(
                Date(
                    entity.task.file()
                        .lastModified()
                )
            )
            Text(
                text = entity.task.file().length()
                    .formatSize() + " | " + lastDate,
                fontSize = 13.sp,
            )
        }
        Button(
            onClick = {
                entity.task.file().open(context)
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFE1E2FF),
                contentColor = Color(0xFF6166C0)
            ),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(text = stringResource(id = R.string.open))
        }
    }
}

@Composable
fun ErrorItem(entity: TaskEntity) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFEAB2A5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FileDownloadOff,
                contentDescription = "Icon",
                tint = MaterialTheme.colors.primary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = entity.task.saveName,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
            )
            Text(
                text = stringResource(id = R.string.download_error),
                fontSize = 13.sp,
            )
        }
        Button(
            onClick = {
                val manager = entity.task.manager(
                    recorder = RoomRecorder(),
                    notificationCreator = DownloadNotificationCreator()
                )
                manager.start()
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFECC8C8),
                contentColor = Color(0xFFC06161)
            ),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}
