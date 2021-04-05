package com.hinnka.tsbrowser.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.hinnka.tsbrowser.App
import java.io.File
import java.io.FileOutputStream

fun Bitmap.encodeToPath(name: String): String? {
    val dir = File(App.instance.filesDir, "thumbnail")
    if (!dir.exists()) {
        dir.mkdir()
    }
    try {
        val file = File(dir, Base64.encodeToString(name.toByteArray(), Base64.URL_SAFE))
        FileOutputStream(file).use {
            compress(Bitmap.CompressFormat.PNG, 70, it)
        }
        return file.path
    } catch (e: Exception) {
        println("encode bitmap failed, $e")
    }
    return null
}

fun String.decodeBitmap(): Bitmap? {
    return try {
        BitmapFactory.decodeFile(this)
    } catch (e: Exception) {
        println("decode bitmap failed, $e")
        null
    }
}