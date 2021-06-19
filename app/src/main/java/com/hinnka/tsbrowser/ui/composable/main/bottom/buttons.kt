package com.hinnka.tsbrowser.ui.composable.main.bottom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.main.drawer.TSDrawer
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState
import com.hinnka.tsbrowser.ui.composable.widget.page.PageController
import com.hinnka.tsbrowser.ui.home.UIState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun TabButton(uiState: MutableState<UIState>) {
    val tabs = TabManager.tabs
    IconButton(onClick = {
        TabManager.currentTab.value?.view?.generatePreview()
        uiState.value = UIState.TabList
    }) {
        when {
            App.isSecretMode -> {
                Box(modifier = Modifier.size(26.dp)) {
                    Box(
                        modifier = Modifier
                            .border(
                                1.5.dp,
                                LocalContentColor.current,
                                RoundedCornerShape(4.dp)
                            )
                            .size(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tabs.size.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W600,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_secret),
                        contentDescription = "Secret Mode",
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colors.surface),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
            Settings.incognitoState.value -> {
                Box(modifier = Modifier.size(26.dp)) {
                    Box(
                        modifier = Modifier
                            .border(
                                1.5.dp,
                                LocalContentColor.current,
                                RoundedCornerShape(4.dp)
                            )
                            .size(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tabs.size.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W600,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_incognito),
                        contentDescription = "incognito",
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colors.surface),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .border(
                            1.5.dp,
                            LocalContentColor.current,
                            RoundedCornerShape(4.dp)
                        )
                        .size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tabs.size.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W600,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun CloseAll() {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clickable {
                TabManager.removeAll()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Default.Close, contentDescription = "Close all")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = stringResource(id = R.string.closeAll))
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun NewTab(uiState: MutableState<UIState>) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
            .clickable {
                TabManager
                    .newTab(context)
                    .apply {
                        goHome()
                        active()
                    }
                uiState.value = UIState.Main
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Default.Add, contentDescription = "New tab")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = stringResource(id = R.string.newtab))
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun CancelButton(uiState: MutableState<UIState>) {
    val viewModel = LocalViewModel.current
    val scope = rememberCoroutineScope()
    IconButton(
        modifier = Modifier.widthIn(56.dp, 72.dp),
        onClick = {
            uiState.value = UIState.Main
            scope.launch {
                //FIXME why need to delay
                delay(50)
                viewModel.addressText.value = TextFieldValue()
            }
        }
    ) {
        Text(text = stringResource(id = R.string.action_cancel))
    }

}


@Composable
fun HomeButton() {
    IconButton(onClick = {
        TabManager.currentTab.value?.goHome()
    }) {
        Icon(imageVector = Icons.Outlined.Home, contentDescription = "Home")
    }
}

@Composable
fun RefreshButton() {
    val tab by TabManager.currentTab
    val progress = tab?.progressState?.value
    IconButton(
        onClick = {
            if (progress == 1f) {
                tab?.view?.reload()
            } else {
                tab?.view?.stopLoading()
            }
        },
        enabled = tab != null
    ) {
        Icon(
            imageVector = if (progress == 1f) Icons.Outlined.Refresh else Icons.Outlined.Close,
            contentDescription = "Refresh"
        )
    }
}

@Composable
fun BookmarkButton() {
    IconButton(onClick = {
        PageController.navigate("bookmarks")
    }) {
        Icon(imageVector = Icons.Outlined.Bookmarks, contentDescription = "Bookmarks")
    }
}

@Composable
fun HistoryButton() {
    IconButton(onClick = {
        PageController.navigate("history")
    }) {
        Icon(imageVector = Icons.Outlined.History, contentDescription = "history")
    }
}

@Composable
fun MoreButton(drawerState: BottomDrawerState) {
    val viewModel = LocalViewModel.current
    val showBadge = DownloadHandler.showDownloadingBadge.value
            || viewModel.canShowDefaultBrowserBadgeState.value
    IconButton(onClick = {
        drawerState.open {
            TSDrawer(drawerState)
        }
    }) {
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
        if (showBadge) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(8.dp)) {
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}