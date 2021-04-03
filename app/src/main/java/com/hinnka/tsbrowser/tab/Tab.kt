package com.hinnka.tsbrowser.tab

import android.graphics.Bitmap
import android.webkit.WebView
import androidx.lifecycle.LiveData
import com.hinnka.tsbrowser.web.TSWebView

data class Tab(
    val id: Int,
    var title: String,
    var icon: Bitmap?,
    var isActive: Boolean = false,
    var view: TSWebView,
) {


    val progressState: LiveData<Float> = view.progressState
    val urlState: LiveData<String?> = view.urlState

    init {

        view.onCreateWindow = { message ->
            message.apply {
                val newWebView = TSWebView(view.context)
                (obj as WebView.WebViewTransport).webView = newWebView
                TabManager.newTab(view.context, newWebView).active()
            }.sendToTarget()
        }

        view.onCloseWindow = {
            TabManager.remove(this)
        }
    }

    fun onBackPressed(): Boolean {
        if (view.canGoBack()) {
            view.goBack()
            return true
        }
        return false
    }
}