package com.hinnka.tsbrowser.ui.composable.main

import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.StatusBar
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.viewmodel.LocalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainPage() {
    val drawerState = rememberBottomDrawerState(initialValue = BottomDrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BottomDrawer(
        drawerContent = { TSDrawer() },
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen
    ) {
        Column {
            StatusBar()
            MainView(drawerState)
        }
        CheckTabs()
        LongPressPopup()
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainView(drawerState: BottomDrawerState) {
    val tab = TabManager.currentTab.value

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
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
    when (uiState.value) {
        UIState.Search -> SearchList()
        UIState.TabList -> TabList()
        else -> {
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
            color = MaterialTheme.colors.secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}