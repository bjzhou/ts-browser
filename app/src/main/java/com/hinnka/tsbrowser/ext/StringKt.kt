package com.hinnka.tsbrowser.ext

import android.R.attr
import android.net.Uri
import android.util.Base64
import android.webkit.URLUtil
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.util.AUTOLINK_WEB_URL
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.R.attr.data
import java.lang.StringBuilder
import java.security.MessageDigest
import kotlin.experimental.and


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

fun String.aesEncode(key: String): String {
    val secretKey = SecretKeySpec(key.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val bytes = cipher.doFinal(this.toByteArray())
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun String.aesDecode(key: String): String {
    val textBytes: ByteArray = Base64.decode(this, Base64.DEFAULT)
    val secretKey = SecretKeySpec(key.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    val bytes = cipher.doFinal(textBytes)
    return String(bytes)
}

fun String?.md5(): String? {
    this ?: return null
    val addSalt = "ts${this}browser"
    val md5 = MessageDigest.getInstance("MD5")
    val bytes = md5.digest(addSalt.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

val String.host: String?
    get() {
        val uri = if (URLUtil.isValidUrl(this)) {
            Uri.parse(this)
        } else {
            Uri.parse("https://$this")
        }
        return uri.host
    }