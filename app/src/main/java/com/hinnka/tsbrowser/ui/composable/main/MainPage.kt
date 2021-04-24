package com.hinnka.tsbrowser.ui.composable.main

import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.ext.screenSize
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.base.StatusBar
import com.hinnka.tsbrowser.ui.base.statusBarHeight
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.viewmodel.LocalViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainPage() {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { StatusBar() },
        drawerContent = { TSDrawer() },
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
    ) {
        MainView(scaffoldState.drawerState)
        CheckTabs()
        LongPressPopup()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainView(drawerState: DrawerState) {
    val tab = TabManager.currentTab.value
    val webViewHeight =
        with(LocalDensity.current) { screenSize().y.toDp() } - 48.dp - statusBarHeight()
    val density = LocalDensity.current
    val addressBarPadding = remember { mutableStateOf(0.dp) }

    val viewModel = LocalViewModel.current
    val uiState = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .weight(1f)
            .nestedScroll(object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    val padding =
                        addressBarPadding.value + with(density) { available.y.toDp() }
                    addressBarPadding.value = max((-56).dp, min(0.dp, padding))
                    logD("onPreScroll $padding")
                    return super.onPreScroll(available, source)
                }
            })) {
            Box(modifier = Modifier.height(webViewHeight)) {
                AndroidView(
                    factory = {
                        FrameLayout(it).apply {
                            setPadding(0, with(density) { 56.dp.toPx() }.toInt(), 0, 0)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
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
        BottomBar()
    }
    when (uiState.value) {
        UIState.Search -> {
            Box(modifier = Modifier.padding(top = 56.dp)) {
                SearchList()
            }
        }
        UIState.TabList -> {
            Box(modifier = Modifier.padding(top = 56.dp)) {
                TabList()
            }
        }
        else -> {
        }
    }
    Box(modifier = Modifier.graphicsLayer {
        translationY = addressBarPadding.value.toPx()
    }) {
        AddressBar(drawerState)
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