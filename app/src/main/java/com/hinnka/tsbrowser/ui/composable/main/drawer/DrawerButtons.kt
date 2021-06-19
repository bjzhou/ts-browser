package com.hinnka.tsbrowser.ui.composable.main.drawer

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.BookmarkType
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel


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