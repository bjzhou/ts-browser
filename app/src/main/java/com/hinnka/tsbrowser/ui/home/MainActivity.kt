package com.hinnka.tsbrowser.ui.home

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme

class MainActivity : BaseActivity() {

    private lateinit var tabContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tabContainer = FrameLayout(this)

        setContent {
            val scaffoldState = rememberScaffoldState()
            TSBrowserTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { tabContainer },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        val tab = TabManager.newTab(this).also { it.active() }
        tabContainer.addView(tab.view)
        tab.view.post {
            tab.view.loadUrl("https://www.baidu.com")
        }
    }
}