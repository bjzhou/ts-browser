package com.hinnka.tsbrowser.ui.composable.main

import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState
import com.hinnka.tsbrowser.ui.composable.widget.StatusBar
import com.hinnka.tsbrowser.ui.composable.widget.TSBackHandler
import com.hinnka.tsbrowser.ui.composable.widget.TSBottomDrawer
import com.hinnka.tsbrowser.ui.home.UIState

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainPage() {
    val drawerState = remember { BottomDrawerState() }
    TSBottomDrawer(
        drawerState = drawerState,
    ) {
        Column {
            StatusBar()
            MainView(drawerState)
        }
        CheckTabs()
        LongPressPopup()
    }
    logD("start MainPage")
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainView(drawerState: BottomDrawerState) {
    val tab = TabManager.currentTab.value

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            TSBackHandler(
                enabled = tab?.canGoBackState?.value == true,
                onBack = { tab?.onBackPressed() }) {
                AndroidView(
                    factory = {
                        FrameLayout(it)
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { tabContainer ->
                        tab?.let {
                            tabContainer.removeAllViews()
                            it.view.removeFromParent()
                            tabContainer.addView(it.view)
                        }
                    }
                )
            }
            ProgressIndicator()
            CoverView()
        }
        BottomBar(drawerState)
    }
}

@Composable
fun CoverView() {
    val viewModel = LocalViewModel.current
    val uiState = viewModel.uiState
    TSBackHandler(
        enabled = uiState.value != UIState.Main,
        onBack = { viewModel.uiState.value = UIState.Main }) {
        when (uiState.value) {
            UIState.Search -> SearchList()
            UIState.TabList -> TabList()
            else -> {
            }
        }
    }
}

@Composable
//FIXME sometimes not work
fun CheckTabs() {
    if (!TabManager.isInitialized) return
    val context = LocalContext.current
    val viewModel = LocalViewModel.current
    val tabs = TabManager.tabs
    if (tabs.isEmpty()) {
        TabManager.newTab(context).apply {
            goHome()
            active()
        }
        if (viewModel.uiState.value != UIState.Main) {
            viewModel.uiState.value = UIState.Main
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProgressIndicator() {
    val currentTab = TabManager.currentTab
    val progressState = currentTab.value?.progressState
    val newProgress = progressState?.value ?: 0f
    val progress: Float = if (newProgress > 0) {
        animateFloatAsState(targetValue = newProgress).value
    } else {
        newProgress
    }
    AnimatedVisibility(visible = progress > 0f && progress < 1f) {
        LinearProgressIndicator(
            progress = progress,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}