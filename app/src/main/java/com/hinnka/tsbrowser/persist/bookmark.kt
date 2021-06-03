package com.hinnka.tsbrowser.persist

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.util.*


private val gson = Gson()

enum class BookmarkType {
    Url, Folder
}

data class Bookmark(
    var guid: String = "",
    var url: String = "",
    var name: String,
    @SerializedName("date_added") var dateAdded: Long = 0,
    @SerializedName("date_modified") var dateModified: Long = 0,
    var type: BookmarkType,
    var children: MutableList<Bookmark> = mutableListOf(),
) {

    @Transient
    var parent: Bookmark? = null
        internal set

    fun remove() {
        parent?.children?.remove(this)
        parent?.dateModified = System.currentTimeMillis()
        parent = null
        list.remove(this)
        save()
    }

    fun addChild(bookmark: Bookmark) {
        if (type == BookmarkType.Url) {
            return
        }
        bookmark.apply {
            if (guid.isBlank()) {
                guid = UUID.randomUUID().toString()
            }
            if (dateAdded == 0L) {
                dateAdded = System.currentTimeMillis()
            } else {
                dateModified = System.currentTimeMillis()
            }
            parent = this@Bookmark
        }
        children.add(bookmark)
        list.add(bookmark)
        if (dateAdded == 0L) {
            dateAdded = System.currentTimeMillis()
        } else {
            dateModified = System.currentTimeMillis()
        }
        save()
    }

    fun update() {
        dateModified = System.currentTimeMillis()
        save()
    }

    companion object {
        val rootPath = File(App.instance.filesDir, "bookmark_${App.processName}")

        var root = Bookmark(
            guid = UUID(0, 0).toString(),
            url = "",
            name = App.instance.getString(R.string.bookmark_root),
            dateAdded = System.currentTimeMillis(),
            type = BookmarkType.Folder
        )
            private set

        private val list = mutableListOf<Bookmark>()

        private fun buildTree(): List<Bookmark> {
            val list = mutableListOf<Bookmark>()
            val folderPool = mutableListOf<Bookmark>()
            var next = root
            while (true) {
                next.children.forEach {
                    it.parent = next
                    list.add(it)
                    if (it.type == BookmarkType.Folder && it.children.isNotEmpty()) {
                        folderPool.add(it)
                    }
                }
                if (folderPool.isEmpty()) {
                    break
                }
                next = folderPool.removeFirst()
            }
            return list
        }

        fun init() {
            if (!rootPath.exists()) return
            try {
                root = FileReader(rootPath).use {
                    gson.fromJson(it, Bookmark::class.java)
                }
            } catch (e: Exception) {
            }
            list.clear()
            list.addAll(buildTree())
        }

        fun findByUrl(url: String): Bookmark? {
            return list.firstOrNull { it.url == url }
        }

        private fun save() {
            ioScope.launch {
                runCatching {
                    val str = gson.toJson(root)
                    rootPath.writeText(str)
                }.onFailure { logE("Bookmark addChild error", throwable = it) }
            }
        }
    }
}