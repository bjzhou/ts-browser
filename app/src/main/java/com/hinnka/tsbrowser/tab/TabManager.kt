package com.hinnka.tsbrowser.tab

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.web.TSWebView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object TabManager {
    private val _tabs = mutableListOf<Tab>()
    private val _currentTab = MutableLiveData<Tab?>()
    private val _tabSize = MutableLiveData(_tabs.size)
    val currentTab: LiveData<Tab?> = _currentTab
    val tabs: List<Tab> = _tabs
    val tabSize: LiveData<Int> = _tabSize

    private var idGenerator = 0

    fun newTab(context: Context, webView: TSWebView = TSWebView(context)): Tab {
        return Tab(
            ++idGenerator,
            App.instance.getString(R.string.newtab),
            null,
            false,
            webView
        ).apply {
            _tabs.add(this)
            _tabSize.postValue(_tabs.size)
        }
    }

    fun remove(id: Int) {
        _tabs.find { it.id == id }?.let {
            remove(it)
        }
        _tabSize.postValue(_tabs.size)
    }

    fun remove(tab: Tab) {
        _tabs.remove(tab)
        tab.view.onDestroy()
        _tabSize.postValue(_tabs.size)
    }

    fun active(tab: Tab) {
        _tabs.forEach {
            if (it == tab) {
                it.isActive = true
                it.view.onResume()
                GlobalScope.launch {
                    _currentTab.postValue(tab)
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