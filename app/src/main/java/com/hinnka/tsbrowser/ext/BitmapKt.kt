package com.hinnka.tsbrowser.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.hinnka.tsbrowser.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun Bitmap.encodeToPath(name: String): String? {
    return withContext(Dispatchers.IO) {
        val dir = File(App.instance.filesDir, "thumbnail")
        if (!dir.exists()) {
            dir.mkdir()
        }
        try {
            val file = File(dir, (if (name.length > 100) { name.substring(0, 100) } else name).replace(":", "").replace("/", "").trim())
            FileOutputStream(file).use {
                compress(Bitmap.CompressFormat.PNG, 70, it)
            }
            return@withContext file.path
        } catch (e: Exception) {
            logE("compress bitmap failed", throwable = e)
        }
        return@withContext null
    }
}

suspend fun String.decodeBitmap(): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            BitmapFactory.decodeFile(this@decodeBitmap)
        } catch (e: Exception) {
            logE("decode bitmap failed", throwable = e)
            null
        }
    }
}