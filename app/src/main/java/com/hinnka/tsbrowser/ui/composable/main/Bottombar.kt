package com.hinnka.tsbrowser.ui.composable.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.hinnka.tsbrowser.tab.TabManager

@Composable
fun BottomBar() {
    Row {
        BackButton()
        ForwardButton()
        HomeButton()
        RefreshButton()
        BookmarkButton()
    }
}

@Composable
fun RowScope.BackButton() {
    val tab by TabManager.currentTab
    IconButton(
        onClick = {
            tab?.onBackPressed()
        },
        modifier = Modifier.weight(1f),
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
        modifier = Modifier.weight(1f),
        enabled = tab?.canGoForwardState?.value == true,
    ) {
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Forward")
    }
}

@Composable
fun RowScope.HomeButton() {
    IconButton(onClick = {
        TabManager.currentTab.value?.goHome()
    }, modifier = Modifier.weight(1f)) {
        Icon(imageVector = Icons.Outlined.Home, contentDescription = "Home")
    }
}

@Composable
fun RowScope.RefreshButton() {
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
        modifier = Modifier.weight(1f),
        enabled = tab != null
    ) {
        Icon(
            imageVector = if (progress == 1f) Icons.Outlined.Refresh else Icons.Outlined.Close,
            contentDescription = "Refresh"
        )
    }
}

@Composable
fun RowScope.BookmarkButton() {
    val added = remember { mutableStateOf(false) }
    IconButton(onClick = {
        added.value = !added.value
    }, modifier = Modifier.weight(1f)) {
        Icon(
            imageVector = if (added.value) Icons.Default.BookmarkAdded else Icons.Outlined.BookmarkAdd,
            contentDescription = "Bookmark",
            tint = if (added.value) MaterialTheme.colors.secondary else MaterialTheme.colors.onPrimary
        )
    }
}