package com.hinnka.tsbrowser.adblock

import android.webkit.WebResourceResponse
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.persist.Settings
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.regex.Pattern

object AdBlocker {

    private val urlList = mutableListOf<String>()
    private val WHITESPACES = Pattern.compile("[ \t]+")

    val emptyResponse: WebResourceResponse by lazy {
        val empty = ByteArrayInputStream(byteArrayOf())
        WebResourceResponse("text/plain", "utf-8", empty)
    }

    init {
        ioScope.launch {
            logD("adblock start load")
            val file = Downloader.getHost()
            logD("adblock file loaded")
            val parseList = mutableListOf<String>()
            file.forEachLine { line ->
                if (line.startsWith("#")) return@forEachLine
                val host = WHITESPACES.split(line)
                if (host.size > 1 && (host[0] == "0.0.0.0" || host[0] == "127.0.0.1")) {
                    parseList.add(host[1])
                }
            }
            urlList.clear()
            urlList.addAll(parseList)
            logD("parse adblock completed")
        }
    }

    fun shouldBlock(host: String): Boolean {
        return Settings.adblock && urlList.any { host.contains(it) }
    }
}