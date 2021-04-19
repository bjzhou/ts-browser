package com.hinnka.tsbrowser.tab

import android.graphics.Bitmap
import android.os.Message
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.mutableStateOf
import com.hinnka.tsbrowser.db.TabInfo
import com.hinnka.tsbrowser.db.update
import com.hinnka.tsbrowser.ext.encodeToPath
import com.hinnka.tsbrowser.web.TSWebView
import com.hinnka.tsbrowser.web.WebDataListener

data class Tab(
    val info: TabInfo,
    var view: TSWebView,
) : WebDataListener {

    var parentTab: Tab? = null

    override val progressState = mutableStateOf(0f)
    override val urlState = mutableStateOf("")
    override val titleState = mutableStateOf("")
    override val iconState = mutableStateOf<Bitmap?>(null)
    override val previewState = mutableStateOf<Bitmap?>(null)
    override val canGoBackState = mutableStateOf(false)
    override val canGoForwardState = mutableStateOf(false)

    override fun onCreateWindow(message: Message) {
        println("TSBrowser onCreateWindow")
        message.apply {
            val newWebView = WebView(view.context)
            newWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val url = request.url
                    TabManager.newTab(view.context).apply {
                        parentTab = this@Tab
                        this.view.isWindow = true
                        loadUrl(url.toString())
                        active()
                    }
                    return true
                }
            }
            (obj as WebView.WebViewTransport).webView = newWebView
        }.sendToTarget()
    }

    override fun onCloseWindow() {
        TabManager.remove(this)
    }

    init {
        view.dataListener = this
    }

    fun loadUrl(url: String) {
        view.post {
            view.loadUrl(url)
        }
    }

    fun goHome() {
        view.post {
            view.loadUrl("https://www.baidu.com")
        }
    }

    fun onResume() {
        view.onResume()
    }

    fun onPause() {
        view.generatePreview()
        view.onPause()
    }

    fun onBackPressed(): Boolean {
        if (view.canGoBack()) {
            view.goBack()
            return true
        }
        parentTab?.let {
            TabManager.remove(this)
            TabManager.active(it)
            return true
        }
        return false
    }

    fun goForward() {
        if (view.canGoForward()) {
            view.goForward()
        }
    }

    override suspend fun updateInfo() {
        info.url = urlState.value
        info.iconPath = iconState.value?.encodeToPath("icon-${info.url}")
        info.thumbnailPath = previewState.value?.encodeToPath("preview-${info.url}")
        info.title = titleState.value
        info.update()
    }
}