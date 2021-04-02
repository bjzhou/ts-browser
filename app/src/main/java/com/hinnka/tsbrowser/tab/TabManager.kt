package com.hinnka.tsbrowser.tab

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.web.TSWebView
import kotlinx.coroutines.flow.collect

object TabManager {
    val tabs = mutableListOf<Tab>()

    private var idGenerator = 0

    fun newTab(context: Context, webView: TSWebView = TSWebView(context)): Tab {
        return Tab(
            ++idGenerator,
            App.instance.getString(R.string.newtab),
            null,
            false,
            webView
        ).apply {
            tabs.add(this)
        }
    }

    fun remove(id: Int) {
        tabs.find { it.id == id }?.let {
            remove(it)
        }
    }

    fun remove(tab: Tab) {
        tabs.remove(tab)
        tab.view.onDestroy()
    }
}

data class Tab(
    val id: Int,
    var title: String,
    var icon: Bitmap?,
    var isActive: Boolean = false,
    var view: TSWebView,
) {
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

        view.lifecycleScope.launchWhenCreated {
            view.titleState.collect { title = it }
            view.iconState.collect { icon = it }
        }
    }
}

fun Tab.active() {
    TabManager.tabs.forEach {
        if (it == this) {
            it.isActive = true
            it.view.onResume()
        } else {
            it.isActive = false
            it.view.onPause()
        }
    }
}