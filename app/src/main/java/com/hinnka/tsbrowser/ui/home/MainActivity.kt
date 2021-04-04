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
import com.hinnka.tsbrowser.ext.tap
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme

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
                            UIState.Main -> MainView()
                            UIState.Search -> Box(modifier = Modifier
                                .fillMaxSize()
                                .tap {
                                    uiState.value = UIState.Main
                                })
                            UIState.TabList -> TabList(uiState)
                        }
                    }
                    CheckTab()
                }
            }
        }
    }

    @Composable
    fun CheckTab() {
        val tabs = TabManager.tabs
        if (tabs.isEmpty()) {
            val tab = TabManager.newTab(this).also { it.active() }
            tab.view.post {
                tab.view.loadUrl("https://www.google.com")
            }
            if (uiState.value != UIState.Main) {
                uiState.value = UIState.Main
            }
        }
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