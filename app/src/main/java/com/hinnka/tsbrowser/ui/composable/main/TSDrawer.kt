package com.hinnka.tsbrowser.ui.composable.main

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.webkit.WebViewFeature
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.persist.*
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.composable.widget.PageController
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun TSDrawer() {
    val context = LocalContext.current
    val showNewPage = TabManager.currentTab.value?.isHome != false
    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        BackButton()
        ForwardButton()
        AddBookmarkButton()
        ShareButton()
    }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x66000000), RoundedCornerShape(8.dp))
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "days",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp),
                    tint = MaterialTheme.colors.primary
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.privacy_protect),
                        fontSize = 13.sp,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = LocalStorage.protectDays.toString(),
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.alignByBaseline(),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(id = R.string.days),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .alignByBaseline(),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "adblock",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp),
                    tint = Color.Red
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.ads_blocked),
                        fontSize = 13.sp,
                    )
                    Row {
                        Text(
                            text = LocalStorage.blockTimesState.value.toString(),
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.alignByBaseline(),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(id = R.string.times),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .alignByBaseline(),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }

    LazyVerticalGrid(cells = GridCells.Fixed(4), modifier = Modifier.padding(8.dp)) {
        drawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DarkMode,
                    contentDescription = "Dark Mode",
                    tint = if (Settings.darkModeState.value) MaterialTheme.colors.primary else LocalContentColor.current.copy(
                        alpha = LocalContentAlpha.current
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.dark),
                    color = if (Settings.darkModeState.value) MaterialTheme.colors.primary else LocalContentColor.current.copy(
                        alpha = LocalContentAlpha.current
                    )
                )
            }
        ) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                Settings.darkMode = !Settings.darkMode
            } else {
                Toast.makeText(context, R.string.dark_unsupport, Toast.LENGTH_SHORT).show()
            }
        }
        if (App.isSecretMode) {
            drawerItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_secret),
                        contentDescription = "Secret Mode",
                        tint = MaterialTheme.colors.primary
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.secret),
                        color = MaterialTheme.colors.primary
                    )
                }
            ) {
                //TODO exit confirm
            }
        } else {
            drawerItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_incognito),
                        contentDescription = "Incognito",
                        tint = if (Settings.incognitoState.value) MaterialTheme.colors.primary else LocalContentColor.current.copy(
                            alpha = LocalContentAlpha.current
                        )
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.incognito),
                        color = if (Settings.incognitoState.value) MaterialTheme.colors.primary else LocalContentColor.current.copy(
                            alpha = LocalContentAlpha.current
                        )
                    )
                }
            ) {
                Settings.incognito = !Settings.incognito
            }
        }
        drawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.FindInPage,
                    contentDescription = "Find in page",
                )
            },
            text = { Text(text = stringResource(id = R.string.find)) },
            enabled = !showNewPage
        ) {
            AlertBottomSheet.open(false) {
                FindInPage()
            }
        }
        drawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DesktopWindows,
                    contentDescription = "Desktop",
                    tint = if (SettingOptions.userAgentDesktop.contains(Settings.userAgentState.value)) MaterialTheme.colors.primary else LocalContentColor.current.copy(
                        alpha = LocalContentAlpha.current
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.desktop),
                    color = if (SettingOptions.userAgentDesktop.contains(Settings.userAgentState.value)) MaterialTheme.colors.primary else LocalContentColor.current.copy(
                        alpha = LocalContentAlpha.current
                    )
                )
            }
        ) {
            Settings.userAgent = if (Settings.userAgentState.value == SettingOptions.userAgentDesktop[0])  Settings.Default.userAgent else SettingOptions.userAgentDesktop[0]
        }
        drawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Bookmarks,
                    contentDescription = "Bookmarks"
                )
            },
            text = { Text(text = stringResource(id = R.string.bookmark)) }
        ) {
            PageController.navigate("bookmarks")
        }
        drawerItem(
            icon = { Icon(imageVector = Icons.Outlined.History, contentDescription = "History") },
            text = { Text(text = stringResource(id = R.string.history)) }
        ) {
            PageController.navigate("history")
        }
        drawerItem(
            icon = {
                Box(Modifier) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = "Downloads"
                    )
                    if (DownloadHandler.showDownloadingBadge.value) {
                        Spacer(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            },
            text = { Text(text = stringResource(id = R.string.downloads)) }
        ) {
            PageController.navigate("downloads")
        }
        drawerItem(
            icon = { Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings") },
            text = { Text(text = stringResource(id = R.string.settings)) }
        ) {
            PageController.navigate("settings")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyGridScope.drawerItem(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    item {
        val scope = rememberCoroutineScope()
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(fontSize = 10.sp),
            LocalContentAlpha provides contentAlpha
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(enabled = enabled) {
                        scope.launch {
                            AlertBottomSheet.close()
                            onClick()
                        }
                    }
                    .padding(16.dp)
            ) {
                icon()
                Spacer(modifier = Modifier.height(4.dp))
                text()
            }
        }
    }
}

@Composable
fun RowScope.BackButton() {
    val tab by TabManager.currentTab
    IconButton(
        onClick = {
            tab?.onBackPressed()
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        enabled = tab?.canGoBackState?.value == true,
    ) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
    }
}

@Composable
fun RowScope.ForwardButton() {
    val tab by TabManager.currentTab
    IconButton(
        onClick = {
            tab?.goForward()
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        enabled = tab?.canGoForwardState?.value == true,
    ) {
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Forward")
    }
}

@Composable
fun RowScope.ShareButton() {
    val viewModel = LocalViewModel.current
    val tab by TabManager.currentTab
    IconButton(
        onClick = {
            viewModel.share(tab?.urlState?.value ?: "")
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
    ) {
        Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share")
    }
}

@Composable
fun RowScope.AddBookmarkButton() {
    val tab by TabManager.currentTab
    val url = tab?.urlState?.value ?: return
    val title = tab?.titleState?.value ?: stringResource(id = R.string.untiled)
    val bookmark = remember { mutableStateOf(Bookmark.findByUrl(url)) }
    IconButton(
        onClick = {
            if (bookmark.value == null) {
                bookmark.value = Bookmark(
                    url = url,
                    name = title,
                    type = BookmarkType.Url
                ).apply {
                    Bookmark.root.addChild(this)
                }
            } else {
                bookmark.value?.remove()
                bookmark.value = null
            }
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
    ) {
        Icon(
            imageVector = if (bookmark.value != null) Icons.Default.BookmarkAdded else Icons.Outlined.BookmarkAdd,
            contentDescription = "Bookmark",
            tint = if (bookmark.value != null) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
        )
    }
}