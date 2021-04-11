package com.hinnka.tsbrowser.tab

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.LiveData
import com.hinnka.tsbrowser.db.TabInfo
import com.hinnka.tsbrowser.db.update
import com.hinnka.tsbrowser.ext.encodeToPath
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.mainScope
import com.hinnka.tsbrowser.web.TSWebView
import kotlinx.coroutines.launch

data class Tab(
    val info: TabInfo,
    var view: TSWebView,
) {

    var parentTab: Tab? = null

    val progressState: LiveData<Float> = view.progressState
    val urlState: LiveData<String?> = view.urlState
    val titleState: LiveData<String?> = view.titleState
    val iconState: LiveData<Bitmap?> = view.iconState
    val previewState: LiveData<Bitmap?> = view.previewState

    init {
        view.onCreateWindow = { message ->
            println("TSBrowser onCreateWindow ${message.target.looper.thread.name}")
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

        view.onCloseWindow = {
            TabManager.remove(this)
        }

        previewState.observeForever {
            ioScope.launch {
                info.url = urlState.value ?: ""
                info.iconPath = iconState.value?.encodeToPath("icon-${info.url}")
                info.thumbnailPath = previewState.value?.encodeToPath("preview-${info.url}")
                info.title = titleState.value ?: ""
                info.update()
            }
        }
    }

    fun loadUrl(url: String) {
        view.post {
            view.loadUrl(url)
        }
    }

    fun goHome() {
        view.post {
            view.loadUrl("https://www.google.com")
        }
    }

    fun onResume() {
        view.onResume()
    }

    fun onPause() {
        view.onPause()
        view.generatePreview()
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
}