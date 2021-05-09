package com.hinnka.tsbrowser.ui.composable.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.*
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.composable.wiget.PageController
import com.hinnka.tsbrowser.ui.theme.PrimaryWhite
import com.hinnka.tsbrowser.viewmodel.LocalViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ColumnScope.TSDrawer() {
    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .background(PrimaryWhite)
    ) {
        BackButton()
        ForwardButton()
        BookmarkButton()
        ShareButton()
    }
    Row(
        modifier = Modifier
            .background(PrimaryWhite)
            .fillMaxWidth()
            .padding(16.dp),
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
        Image(
            painter = painterResource(id = R.drawable.ic_security),
            contentDescription = "Security"
        )
    }
    ListItem(
        modifier = Modifier.clickable {
            PageController.navigate("bookmarks")
        },
        icon = { Icon(imageVector = Icons.Outlined.Bookmarks, contentDescription = "Bookmarks") },
    ) {
        Text(text = stringResource(id = R.string.bookmark))
    }
    ListItem(
        modifier = Modifier.clickable { },
        icon = { Icon(imageVector = Icons.Outlined.History, contentDescription = "History") },
    ) {
        Text(text = stringResource(id = R.string.history))
    }
    ListItem(
        modifier = Modifier.clickable {
            PageController.navigate("downloads")
        },
        icon = { Icon(imageVector = Icons.Outlined.Download, contentDescription = "Downloads") },
    ) {
        Text(text = stringResource(id = R.string.downloads))
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
fun RowScope.BookmarkButton() {
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