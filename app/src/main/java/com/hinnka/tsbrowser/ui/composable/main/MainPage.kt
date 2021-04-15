package com.hinnka.tsbrowser.ui.composable.main

import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import com.hinnka.tsbrowser.db.update
import com.hinnka.tsbrowser.ext.encodeToPath
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.viewmodel.LocalViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainPage() {
    val viewModel = LocalViewModel.current
    val scaffoldState = rememberScaffoldState()
    val addressBarVisible = viewModel.addressBarVisible
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val slop = ViewConfiguration.get(context).scaledTouchSlop
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

@Composable
fun MainView() {
    val owner = LocalLifecycleOwner.current
    val tab = TabManager.currentTab.value
    Box(modifier = Modifier.fillMaxSize()) {
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
    }
}

@Composable
fun CheckTab() {
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