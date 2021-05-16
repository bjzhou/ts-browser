package com.hinnka.tsbrowser.persist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import com.hinnka.tsbrowser.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object IconCache : LruCache<String, Bitmap?>(99) {

    private val iconDir = File(App.instance.filesDir, "icons").apply {
        if (!exists()) {
            mkdir()
        }
    }

    override fun entryRemoved(
        evicted: Boolean,
        key: String,
        oldValue: Bitmap?,
        newValue: Bitmap?
    ) {
        if (newValue == null) {
            File(iconDir, generateName(key)).delete()
        }
    }

    override fun create(key: String): Bitmap? {
        val name = generateName(key)
        return try {
            BitmapFactory.decodeFile(File(iconDir, name).path)
        } catch (e: Exception) {
            null
        }
    }

    override fun sizeOf(key: String, value: Bitmap?): Int {
        return 1
    }

    private fun generateName(key: String): String {
        return Base64.encodeToString(key.toByteArray(), Base64.URL_SAFE).trim()
    }

    suspend fun asyncGet(key: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            super.get(key)
        }
    }

    suspend fun save(key: String, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val name = generateName(key)
            FileOutputStream(File(iconDir, name)).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
            }
        }
    }
}