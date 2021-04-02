package com.hinnka.tsbrowser.download

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.text.format.Formatter.formatFileSize
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.mimeType
import com.hinnka.tsbrowser.ui.base.BaseActivity
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import zlc.season.rxdownload4.RANGE_CHECK_HEADER
import zlc.season.rxdownload4.download
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.task.Task
import java.io.File
import java.io.FileInputStream

class DownloadHandler(val context: Context) : DownloadListener {

    init {
        OpenReceiver.register(context)
    }

    override fun onDownloadStart(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimetype: String?,
        contentLength: Long
    ) {
        if (tryOpenStream(url, contentDisposition, mimetype)) {
            return
        }

        val guessName = URLUtil.guessFileName(url, contentDisposition, mimetype)
        val downloadSize = if (contentLength > 0) {
            formatFileSize(context, contentLength)
        } else {
            context.getString(R.string.unknown_size)
        }

        requestPermissionIfNeeded {
            AlertDialog.Builder(context).apply {
                setMessage(context.getString(R.string.download_message, guessName, downloadSize))
                setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        download(url, guessName, mimetype)
                    } catch (e: Exception) {
                    }
                }
                setNegativeButton(android.R.string.cancel) { _, _ ->
                }
            }.show()
        }
    }

    private fun tryOpenStream(
        url: String,
        contentDisposition: String?,
        mimetype: String?
    ): Boolean {
        val type = mimetype ?: return false
        if (contentDisposition == null
            || !contentDisposition.regionMatches(0, "attachment", 0, 10, true)
        ) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(url), type)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
            }
        }
        return false
    }

    private fun getTask(url: String): Task {
        val savePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.filesDir, Environment.DIRECTORY_DOWNLOADS)
        } else {
            File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
        }
        if (!savePath.exists()) {
            savePath.mkdir()
        }
        return Task(
            url = url,
            savePath = savePath.path
        )
    }

    private fun download(url: String, guessName: String, mimetype: String?) {
        val headerMap = RANGE_CHECK_HEADER.toMutableMap()
        val cookie = CookieManager.getInstance().getCookie(url)
        if (!cookie.isNullOrEmpty()) {
            headerMap["Cookie"] = cookie
        }
        val disposable = getTask(url).download(headerMap)
            .observeOn(Schedulers.io())
            .subscribeBy(
                onError = {
                    println("TSBrowser, download error: $it")
                },
                onNext = {
                    println("TSBrowser, download state: ${it.percent()}")
                    DownloadNotification.notify(url, guessName, it.percent().toInt())
                },
                onComplete = {
                    println("TSBrowser, download finished")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        addToPublicDownloadDir(url)
                    }
                    DownloadNotification.notifyComplete(url)
                }
            )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addToPublicDownloadDir(url: String) {
        val resolver = context.contentResolver ?: return
        val file = url.file()
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val cursor = resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.TITLE
            ),
            "${MediaStore.Downloads.TITLE}='${file.name}' or ${MediaStore.Downloads.TITLE}='${file.nameWithoutExtension}'",
            null,
            null
        )

        val downloadDetail = ContentValues().apply {
            put(MediaStore.Downloads.TITLE, file.name)
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.MIME_TYPE, file.mimeType)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = if (cursor != null && cursor.count > 0) {
            cursor.use { c ->
                c.moveToFirst()
                val id = c.getLong(c.getColumnIndex(MediaStore.Downloads._ID))
                ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
            }
        } else {
            resolver.insert(collection, downloadDetail) ?: return
        }
        resolver.openFileDescriptor(uri, "w", null)?.use { fd ->
            ParcelFileDescriptor.AutoCloseOutputStream(fd).use {
                val fis = FileInputStream(file)
                fis.channel.transferTo(0, file.length(), it.channel)
            }
        }
        downloadDetail.clear()
        downloadDetail.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, downloadDetail, null, null)
    }

    private fun requestPermissionIfNeeded(completion: () -> Unit) {
        val activity = context as? BaseActivity
        activity?.let {
            it.lifecycleScope.launchWhenCreated {
                it.requestPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                completion()
            }
        } ?: completion()
    }
}