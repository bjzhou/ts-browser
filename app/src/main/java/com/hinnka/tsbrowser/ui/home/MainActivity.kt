package com.hinnka.tsbrowser.ui.home

import android.os.Bundle
import android.view.ViewConfiguration
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.lifecycleScope
import com.hinnka.tsbrowser.db.update
import com.hinnka.tsbrowser.ext.encodeToPath
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme
import com.hinnka.tsbrowser.viewmodel.HomeViewModel
import com.hinnka.tsbrowser.viewmodel.LocalViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

open class MainActivity : BaseActivity() {

    private var slop = 0
    val viewModel by viewModels<HomeViewModel>()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        slop = ViewConfiguration.get(this@MainActivity).scaledTouchSlop

        setContent {
            val scaffoldState = rememberScaffoldState()
            val addressBarVisible = viewModel.addressBarVisible
            val uiState = viewModel.uiState

            Providers {
                TSBrowserTheme {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            AnimatedVisibility(
                                visible = addressBarVisible.value,
                                enter = fadeIn() + slideInVertically() + expandIn(initialSize = { IntSize(it.width, 0) }),
                                exit = fadeOut() + slideOutVertically() + shrinkOut(targetSize = { IntSize(it.width, 0) })
                            ) {
                                AddressBar()
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
                                UIState.Search -> SearchList()
                                UIState.TabList -> {
                                    TabManager.currentTab.value?.onPause()
                                    TabList()
                                }
                            }
                        }
                        CheckTab()
                    }
                }
            }
        }
        
        lifecycleScope.launchWhenCreated {
            TabManager.loadTabs(this@MainActivity)
        }
    }

    @Composable
    fun Providers(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalViewModel provides viewModel, content = content)
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
            if (viewModel.uiState.value != UIState.Main) {
                viewModel.uiState.value = UIState.Main
            }
        }
        val current = TabManager.currentTab
        current.value?.let { tab ->
            tab.previewState.value?.let {
                ioScope.launch {
                    tab.info.url = tab.urlState.value ?: ""
                    tab.info.iconPath = tab.iconState.value?.encodeToPath("icon-${tab.info.url}")
                    tab.info.thumbnailPath = tab.previewState.value?.encodeToPath("preview-${tab.info.url}")
                    tab.info.title = tab.titleState.value ?: ""
                    tab.info.update()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        TabManager.onResume(viewModel.uiState.value)
    }

    override fun onPause() {
        super.onPause()
        TabManager.onPause()
    }

    override fun onBackPressed() {
        if (viewModel.uiState.value != UIState.Main) {
            viewModel.uiState.value = UIState.Main
            return
        }
        if (TabManager.currentTab.value?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}