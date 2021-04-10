package com.hinnka.tsbrowser.tab

import android.content.Context
import android.os.Parcel
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.db.TabEntity
import com.hinnka.tsbrowser.db.Tabs
import com.hinnka.tsbrowser.ext.decodeBitmap
import com.hinnka.tsbrowser.ext.encodeToPath
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.mainScope
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.web.TSWebView
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TabManager {
    val tabs = mutableStateListOf<Tab>()
    val currentTab = MutableLiveData<Tab?>()
    var isInitialized = false
        private set

    private var idGenerator = 0

    fun newTab(context: Context, webView: TSWebView = TSWebView(context)): Tab {
        return Tab(
            ++idGenerator,
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

    fun removeAll() {
        tabs.forEach { it.view.onDestroy() }
        tabs.clear()
    }

    fun active(tab: Tab) {
        if (tab.isActive) {
            return
        }
        tabs.forEach {
            if (it == tab) {
                it.isActive = true
                it.onResume()
                GlobalScope.launch {
                    currentTab.postValue(tab)
                }
            } else {
                it.isActive = false
                it.onPause()
            }
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
        saveTabs()
    }

    fun saveTabs() {
        ioScope.launch {
            val mmkv = MMKV.defaultMMKV()
            val tabs = Tabs(tabs.map {
                val url = it.urlState.value ?: ""
                TabEntity(
                    it.id,
                    it.isActive,
                    it.titleState.value ?: "",
                    it.iconState.value?.encodeToPath("icon-$url"),
                    url,
                    it.previewState.value?.encodeToPath("preview-$url")
                )
            })
            mmkv?.encode("tabs-${App.getProcessName()}", tabs)
        }
    }

    fun loadTabs(context: Context) {
        if (isInitialized) return
        ioScope.launch {
            val mmkv = MMKV.defaultMMKV()
            val tabs = mmkv?.decodeParcelable("tabs-${App.getProcessName()}", Tabs::class.java)
            val savedTabs = tabs?.list?.map {
                val webview = withContext(Dispatchers.Main) {
                    TSWebView(context)
                }
                Tab(it.id, it.isActive, webview).apply {
                    view.titleState.postValue(it.title)
                    view.iconState.postValue(it.iconPath?.decodeBitmap())
                    view.previewState.postValue(it.thumbnailPath?.decodeBitmap())
                    if (isActive) {
                        currentTab.postValue(this)
                    }
                    view.post {
                        view.loadUrl(it.url)
                    }
                }
            }
            TabManager.tabs.clear()
            if (savedTabs != null) {
                TabManager.tabs.addAll(savedTabs)
            } else {
                mainScope.launch {
                    newTab(context).apply {
                        goHome()
                        active()
                    }
                }
            }
            isInitialized = true
        }
    }

}

fun Tab.active() {
    TabManager.active(this)
}