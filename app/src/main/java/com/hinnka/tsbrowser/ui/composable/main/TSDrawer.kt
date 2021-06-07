package com.hinnka.tsbrowser.ui.composable.main

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.BookmarkType
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.widget.PageController
import com.hinnka.tsbrowser.ui.theme.primaryLight


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun TSDrawer() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
//            .background(MaterialTheme.colors.primaryLight)
    ) {
        BackButton()
        ForwardButton()
        AddBookmarkButton()
        ShareButton()
    }
    Row(
        modifier = Modifier
//            .background(MaterialTheme.colors.primaryLight)
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.privacy_protect),
                fontSize = 12.sp,
            )
            Text(
                text = stringResource(id = R.string.days, 101),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Icon(
            imageVector = Icons.Outlined.Security,
            contentDescription = "Security",
            modifier = Modifier.size(68.dp),
            tint = MaterialTheme.colors.primary
        )
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
        if (!App.isSecretMode) {
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
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Downloads"
                )
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
    onClick: () -> Unit
) {
    item {
        CompositionLocalProvider(LocalTextStyle provides TextStyle(fontSize = 10.sp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        onClick()
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