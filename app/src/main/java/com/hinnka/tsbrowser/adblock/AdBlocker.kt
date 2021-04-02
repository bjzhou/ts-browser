package com.hinnka.tsbrowser.adblock

import android.net.Uri
import android.webkit.WebResourceResponse
import com.hinnka.tsbrowser.App
import java.io.ByteArrayInputStream

object AdBlocker {

    private val urlList: List<String> by lazy {
        String(App.instance.assets.open("blockhosts.txt").readBytes()).split("\n").filter { it.isNotBlank() }
    }

    val emptyResponse: WebResourceResponse by lazy {
        val empty = ByteArrayInputStream(byteArrayOf())
        WebResourceResponse("text/plain", "utf-8", empty)
    }

    fun shouldBlock(url: Uri): Boolean {
        return urlList.any { url.host?.contains(it) == true }
    }
}