package com.hinnka.tsbrowser.ui.composable.main

import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.main.bottom.BottomBar
import com.hinnka.tsbrowser.ui.composable.welcome.SecretWelcome
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState
import com.hinnka.tsbrowser.ui.composable.widget.StatusBar
import com.hinnka.tsbrowser.ui.composable.widget.TSBackHandler
import com.hinnka.tsbrowser.ui.composable.widget.TSBottomDrawer
import com.hinnka.tsbrowser.ui.home.UIState
import kotlinx.coroutines.delay

val LocalMainDrawerState = staticCompositionLocalOf<BottomDrawerState> {
    error("main drawer state not found")
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainPage() {
    logD("MainPage start")
    val mainDrawerState = remember { BottomDrawerState() }

    TSBottomDrawer(drawerState = mainDrawerState) {
        CompositionLocalProvider(LocalMainDrawerState provides mainDrawerState) {
            Column {
                StatusBar()
                MainView()
            }
        }
    }
    LongPressPopup()
    Welcome()
    logD("MainPage end")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Welcome() {
    var showSecret by remember { mutableStateOf(false) }
    val mnemonicNotSet = Settings.mnemonicState.value == null
    LaunchedEffect(key1 = mnemonicNotSet) {
        delay(500)
        showSecret = mnemonicNotSet
    }
    AnimatedVisibility(
        visible = showSecret,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            spring(stiffness = 250f)
        ),
        exit = fadeOut() + slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            spring(stiffness = 250f)
        )
    ) {
        TSBackHandler(onBack = {}) {
            SecretWelcome()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainView() {
    logD("MainView start")
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
            NewTabView()
            CoverView()
        }
        BottomBar()
    }
    logD("MainView end")
}

@Composable
fun NewTabView() {
    val tab = TabManager.currentTab.value ?: return
    val viewModel = LocalViewModel.current
    val uiState = viewModel.uiState
    if (uiState.value == UIState.Main && tab.isHome) {
        NewTabPage()
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