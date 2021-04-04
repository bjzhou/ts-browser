package com.hinnka.tsbrowser.tab

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import com.hinnka.tsbrowser.web.TSWebView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object TabManager {
    val tabs = mutableStateListOf<Tab>()
    val currentTab = MutableLiveData<Tab?>()

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
                it.view.onResume()
                GlobalScope.launch {
                    currentTab.postValue(tab)
                }
            } else {
                it.isActive = false
                it.view.onPause()
            }
        }
    }

}

fun Tab.active() {
    TabManager.active(this)
}