package com.hinnka.tsbrowser.ui.home

import android.os.Bundle
import android.view.ViewConfiguration
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.lifecycleScope
import com.hinnka.tsbrowser.ext.tap
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme
import kotlin.math.abs

open class MainActivity : BaseActivity() {

    private val uiState = mutableStateOf(UIState.Main)
    private val addressBarVisible = mutableStateOf(true)
    private var slop = 0

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        slop = ViewConfiguration.get(this@MainActivity).scaledTouchSlop

        setContent {
            val scaffoldState = rememberScaffoldState()

            TSBrowserTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        AnimatedVisibility(
                            visible = addressBarVisible.value,
                            enter = fadeIn() + slideInVertically() + expandIn(initialSize = { IntSize(it.width, 0) }),
                            exit = fadeOut() + slideOutVertically() + shrinkOut(targetSize = { IntSize(it.width, 0) })
                        ) {
                            AddressBar(uiState)
                        }
                    },
                    modifier = Modifier.pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < 0 && abs(dragAmount) >= slop) {
                                addressBarVisible.value = false
                            } else if (dragAmount > 0 && dragAmount >= slop) {
                                addressBarVisible.value = true
                            }
                        }
                    }
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
        
        lifecycleScope.launchWhenCreated {
            TabManager.loadTabs(this@MainActivity)
        }
    }

    @Composable
    fun CheckTab() {
        if (!TabManager.isInitialized) return
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