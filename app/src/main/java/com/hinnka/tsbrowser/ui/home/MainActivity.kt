package com.hinnka.tsbrowser.ui.home

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme
import kotlinx.coroutines.flow.collect

class MainActivity : BaseActivity() {

    private lateinit var tabContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tabContainer = FrameLayout(this)

        setContent {
            val scaffoldState = rememberScaffoldState()
            val searchState = mutableStateOf(false)

            TSBrowserTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = { AddressBar(searchState) },
                ) {
                    MainView(tabContainer)
                }
            }
        }

        if (TabManager.tabSize.value == 0) {
            val tab = TabManager.newTab(this).also { it.active() }
            tab.view.post {
                tab.view.loadUrl("https://www.google.com")
            }
        }

        TabManager.currentTab.observe(this) { t ->
            t?.let {
                tabContainer.removeAllViews()
                it.view.removeFromParent()
                tabContainer.addView(it.view)
            }
        }
    }

    override fun onBackPressed() {
        if (TabManager.currentTab.value?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}