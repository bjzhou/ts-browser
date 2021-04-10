package com.hinnka.tsbrowser.tab

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.LiveData
import com.hinnka.tsbrowser.web.TSWebView

data class Tab(
    val id: Int,
    var isActive: Boolean = false,
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
}