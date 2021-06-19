package com.hinnka.tsbrowser.ui.composable.main.drawer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.webkit.WebViewFeature
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.persist.SettingOptions
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState
import com.hinnka.tsbrowser.ui.composable.widget.page.PageController
import kotlinx.coroutines.launch


@Composable
fun DarkModeItem() {
    val context = LocalContext.current
    DrawerItem(
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
}

@Composable
fun SecretItem() {
    DrawerItem(
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
}

@Composable
fun IncognitoItem() {
    DrawerItem(
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

@Composable
fun FindInPageItem(drawerState: BottomDrawerState) {
    val showNewPage = TabManager.currentTab.value?.isHome != false
    val scope = rememberCoroutineScope()
    DrawerItem(
        icon = {
            Icon(
                imageVector = Icons.Outlined.FindInPage,
                contentDescription = "Find in page",
            )
        },
        text = { Text(text = stringResource(id = R.string.find)) },
        enabled = !showNewPage
    ) {
        scope.launch {
            drawerState.close()
            drawerState.open(false) {
                FindInPage(drawerState)
            }
        }
    }
}

@Composable
fun DesktopItem() {
    DrawerItem(
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
        Settings.userAgent =
            if (Settings.userAgentState.value == SettingOptions.userAgentDesktop[0]) Settings.Default.userAgent else SettingOptions.userAgentDesktop[0]
    }
}

@Composable
fun BookmarksItem() {
    DrawerItem(
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
}

@Composable
fun HistoryItem() {
    DrawerItem(
        icon = { Icon(imageVector = Icons.Outlined.History, contentDescription = "History") },
        text = { Text(text = stringResource(id = R.string.history)) }
    ) {
        PageController.navigate("history")
    }
}

@Composable
fun DownloadsItem() {
    DrawerItem(
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
}

@Composable
fun SettingsItem() {
    val viewModel = LocalViewModel.current
    DrawerItem(
        icon = {
            Box {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings")
                if (viewModel.canShowDefaultBrowserBadgeState.value) {
                    Spacer(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        },
        text = { Text(text = stringResource(id = R.string.settings)) }
    ) {
        PageController.navigate("settings")
    }
}

@Composable
fun DrawerItem(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
    CompositionLocalProvider(
        LocalTextStyle provides TextStyle(fontSize = 10.sp),
        LocalContentAlpha provides contentAlpha
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(enabled = enabled) {
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