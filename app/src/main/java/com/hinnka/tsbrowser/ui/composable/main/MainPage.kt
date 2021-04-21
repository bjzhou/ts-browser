package com.hinnka.tsbrowser.ui.composable.main

import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.StatusBar
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.viewmodel.LocalViewModel
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
            Column {
                StatusBar()
                AnimatedVisibility(
                    visible = addressBarVisible.value,
                    enter = fadeIn() + slideInVertically() + expandIn(initialSize = { IntSize(it.width, 0) }),
                    exit = fadeOut() + slideOutVertically() + shrinkOut(targetSize = { IntSize(it.width, 0) })
                ) {
                    AddressBar(scaffoldState.drawerState)
                }
            }
        },
        drawerContent = { TSDrawer() },
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
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
        Box {
            MainView()
            when (uiState.value) {
                UIState.Search -> {
                    SearchList()
                }
                UIState.TabList -> {
                    TabList()
                }
                else -> {}
            }
        }
        CheckTabs()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainView() {
    Column(modifier = Modifier.fillMaxSize()) {
        val tab = TabManager.currentTab.value
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
        }
        BottomBar()
    }
    LongPressPopup()
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