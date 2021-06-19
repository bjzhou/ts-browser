package com.hinnka.tsbrowser.ui.composable.main.bottom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.main.LocalMainDrawerState
import com.hinnka.tsbrowser.ui.composable.widget.Center
import com.hinnka.tsbrowser.ui.home.UIState

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun BottomBar() {
    val viewModel = LocalViewModel.current
    val tab = TabManager.currentTab.value
    val uiState = viewModel.uiState
    val drawerState = LocalMainDrawerState.current

    Column(modifier = Modifier.graphicsLayer {
        translationY = -viewModel.imeHeightState.value
    }) {
        val showNewPage = uiState.value == UIState.Main && tab?.isHome != false
        AnimatedVisibility(visible = showNewPage) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(56.dp)
                    .background(MaterialTheme.colors.surface),
            ) {
                Center(modifier = Modifier.weight(1f)) {
                    TabButton(uiState)
                }
                Center(modifier = Modifier.weight(1f)) {
                    BookmarkButton()
                }
                Center(modifier = Modifier.weight(1f)) {
                    HistoryButton()
                }
                Center(modifier = Modifier.weight(1f)) {
                    MoreButton(drawerState)
                }
            }
        }
        AnimatedVisibility(visible = !showNewPage) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(56.dp)
                    .background(MaterialTheme.colors.surface)
                    .padding(start = if (uiState.value == UIState.Search) 8.dp else 0.dp),
            ) {
                AnimatedVisibility(visible = uiState.value == UIState.Main) {
                    HomeButton()
                }
                AnimatedVisibility(visible = uiState.value == UIState.Main) {
                    TabButton(uiState)
                }
                AnimatedVisibility(visible = uiState.value == UIState.TabList) {
                    CloseAll()
                }
                AnimatedVisibility(visible = uiState.value == UIState.TabList) {
                    NewTab(uiState)
                }
                AnimatedVisibility(visible = true, modifier = Modifier.weight(1f)) {
                    if (uiState.value != UIState.TabList) {
                        AddressTextField(
                            modifier = Modifier.fillMaxSize(),
                            uiState = uiState
                        )
                    }
                }
                AnimatedVisibility(visible = uiState.value == UIState.Search) {
                    CancelButton(uiState)
                }
                AnimatedVisibility(visible = uiState.value == UIState.Main) {
                    RefreshButton()
                }
                AnimatedVisibility(visible = uiState.value != UIState.Search) {
                    MoreButton(drawerState)
                }
            }
        }
    }
}
