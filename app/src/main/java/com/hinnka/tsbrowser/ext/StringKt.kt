package com.hinnka.tsbrowser.ext

import android.net.Uri
import android.webkit.URLUtil
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.util.AUTOLINK_WEB_URL

fun String.toUrl(): String {
    if (isUrl()) {
        return URLUtil.guessUrl(this.trim())
    }
    return toSearchUrl()
}

fun String.isUrl(): Boolean {
    val inUrl = this.trim()
    if (inUrl.contains(" ")) {
        return false
    }
    if (URLUtil.isValidUrl(inUrl)) {
        return true
    }
    val matcher = AUTOLINK_WEB_URL.matcher(inUrl)
    if (matcher.matches()) {
        return true
    }
    return false
}

fun String.toSearchUrl(): String {
    return String.format(Settings.searchEngine.value, this)
}

val String.host: String?
    get() = Uri.parse(this).host