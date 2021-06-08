package com.hinnka.tsbrowser.tab

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.hinnka.tsbrowser.persist.AppDatabase
import com.hinnka.tsbrowser.persist.TabInfo
import com.hinnka.tsbrowser.persist.delete
import com.hinnka.tsbrowser.persist.update
import com.hinnka.tsbrowser.ext.decodeBitmap
import com.hinnka.tsbrowser.ext.host
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.persist.IconMap
import com.hinnka.tsbrowser.web.TSWebView
import kotlinx.coroutines.launch

object TabManager {
    val tabs = mutableStateListOf<Tab>()
    val currentTab = mutableStateOf<Tab?>(null)
    var isInitialized = false
        private set

    fun newTab(context: Context, webView: TSWebView = TSWebView(context)): Tab {
        val info = TabInfo()
        return Tab(info, webView).apply {
            ioScope.launch {
                AppDatabase.instance.tabDao().insert(info).apply { info.id = this }
            }
            tabs.add(this)
        }
    }

    fun remove(id: Long) {
        tabs.find { it.info.id == id }?.let {
            remove(it)
        }
    }

    fun remove(tab: Tab) {
        tabs.remove(tab)
        tab.view.onDestroy()
        tab.info.delete()
    }

    fun removeAll() {
        tabs.forEach {
            it.view.onDestroy()
            it.info.delete()
        }
        tabs.clear()
    }

    fun active(tab: Tab) {
        if (tab.info.isActive) {
            return
        }
        tabs.forEach {
            if (it == tab) {
                it.info.isActive = true
                it.onResume()
                currentTab.value = tab
            } else {
                it.info.isActive = false
                it.onPause()
            }
            it.info.update()
        }
    }

    fun onResume(uiState: UIState) {
        currentTab.value?.view?.resumeTimers()
        if (uiState == UIState.Main) {
            currentTab.value?.onResume()
        }
    }

    fun onPause() {
        currentTab.value?.onPause()
        currentTab.value?.view?.pauseTimers()
    }

    suspend fun loadTabs(context: Context) {
        if (isInitialized) return
        logD("loadTabs start")
        val savedTabs = AppDatabase.instance.tabDao().getAll().map {
            logD("loadTabs 1")
            Tab(it, TSWebView(context)).apply {
                logD("loadTabs 2")
                urlState.value = it.url
                titleState.value = it.title
                ioScope.launch {
                    previewState.value = it.thumbnailPath?.decodeBitmap()
                }
                if (it.isActive) {
                    currentTab.value = this
                }
                view.post {
                    view.loadUrl(it.url)
                }
                logD("loadTabs 3")
            }
        }
        tabs.clear()
        tabs.addAll(savedTabs)
        isInitialized = true
        if (tabs.isEmpty()) {
            logD("loadTabs 4")
            newTab(context).apply {
                goHome()
                active()
            }
        }
        logD("loadTabs end")
    }

}

fun Tab.active() {
    TabManager.active(this)
}