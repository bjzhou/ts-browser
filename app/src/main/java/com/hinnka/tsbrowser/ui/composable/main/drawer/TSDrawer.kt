package com.hinnka.tsbrowser.ui.composable.main.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TSDrawer(drawerState: BottomDrawerState) {
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

    PrivacyContent()

    LazyVerticalGrid(cells = GridCells.Fixed(4), modifier = Modifier.padding(8.dp)) {
        item { DarkModeItem() }
        item {
            if (App.isSecretMode) {
                SecretItem()
            } else {
                IncognitoItem()
            }
        }
        item { FindInPageItem(drawerState = drawerState) }
        item { DesktopItem() }
        item { BookmarksItem() }
        item { HistoryItem() }
        item { DownloadsItem() }
        item { SettingsItem() }
    }
}
