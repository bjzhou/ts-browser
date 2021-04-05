package com.hinnka.tsbrowser.ui.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.hinnka.tsbrowser.db.Tabs
import com.hinnka.tsbrowser.ext.tap
import com.hinnka.tsbrowser.tab.Tab
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme
import com.hinnka.tsbrowser.web.TSWebView
import com.tencent.mmkv.MMKV

class MainActivity : BaseActivity() {

    private val uiState = mutableStateOf(UIState.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberScaffoldState()

            TSBrowserTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = { AddressBar(uiState) },
                ) {
                    Crossfade(targetState = uiState.value) {
                        when (uiState.value) {
                            UIState.Main -> {
                                TabManager.currentTab.value?.onResume()
                                MainView()
                            }
                            UIState.Search -> Box(modifier = Modifier
                                .fillMaxSize()
                                .tap {
                                    uiState.value = UIState.Main
                                })
                            UIState.TabList -> {
                                TabManager.currentTab.value?.onPause()
                                TabList(uiState)
                            }
                        }
                    }
                    CheckTab()
                }
            }
        }

        TabManager.loadTabs(this)
    }

    @Composable
    fun CheckTab() {
        val tabs = TabManager.tabs
        if (tabs.isEmpty()) {
            TabManager.newTab(this).apply {
                goHome()
                active()
            }
            if (uiState.value != UIState.Main) {
                uiState.value = UIState.Main
            }
        }
    }

    override fun onResume() {
        super.onResume()
        TabManager.onResume(uiState.value)
    }

    override fun onPause() {
        super.onPause()
        TabManager.onPause()
    }

    override fun onBackPressed() {
        if (uiState.value != UIState.Main) {
            uiState.value = UIState.Main
            return
        }
        if (TabManager.currentTab.value?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}