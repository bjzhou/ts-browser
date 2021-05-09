package com.hinnka.tsbrowser.tab

import android.graphics.Bitmap
import android.os.Message
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.mutableStateOf
import com.hinnka.tsbrowser.persist.TabInfo
import com.hinnka.tsbrowser.persist.update
import com.hinnka.tsbrowser.ext.encodeToPath
import com.hinnka.tsbrowser.ext.host
import com.hinnka.tsbrowser.ui.home.LongPressInfo
import com.hinnka.tsbrowser.util.IconCache
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
    override val longPressState = mutableStateOf(LongPressInfo())

    override fun onCreateWindow(message: Message) {
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
        urlState.value = url
        view.post {
            view.loadUrl(url)
        }
    }

    val isHome
        get() = urlState.value == "https://www.baidu.com"

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
        iconState.value?.let {
            info.url.host?.let { it1 -> IconCache.save(it1, it) }
        }
        info.thumbnailPath = previewState.value?.encodeToPath("preview-${info.url}")
        info.title = titleState.value
        info.update()
    }
}