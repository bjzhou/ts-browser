package com.hinnka.tsbrowser.web

import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView

interface UIController {
    fun onProgressChanged(progress: Int)
    fun onReceivedTitle(title: String?)
    fun onReceivedIcon(icon: Bitmap?)
    fun onShowCustomView(view: View, requestedOrientation: Int, callback: WebChromeClient.CustomViewCallback)
    fun onHideCustomView()
    fun onCreateWindow(resultMsg: Message): Boolean
    fun onCloseWindow()
    suspend fun requestPermissions(vararg permissions: String): Map<String, Boolean>
    suspend fun showFileChooser(fileChooserParams: WebChromeClient.FileChooserParams): Array<Uri>?
    fun onPageStarted(url: String, favicon: Bitmap?)
    fun onPageFinished(url: String)
    fun doUpdateVisitedHistory(url: String, isReload: Boolean)
}

suspend fun UIController.requestPermission(permission: String): Boolean {
    val map = requestPermissions(permission)
    return map[permission] == true
}