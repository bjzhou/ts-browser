package com.hinnka.tsbrowser.ui.composable.bookmark

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.BookmarkType
import com.hinnka.tsbrowser.ext.host
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import com.hinnka.tsbrowser.ui.composable.widget.TSBackHandler
import com.hinnka.tsbrowser.persist.IconMap
import com.hinnka.tsbrowser.ui.composable.widget.page.PageController

@Composable
fun BookmarkPage() {
    val currentFolder = remember { mutableStateOf(Bookmark.root) }
    TSBackHandler(
        enabled = currentFolder.value.parent != null,
        onBack = {
            currentFolder.value.parent?.let {
                currentFolder.value = it
            }
        }
    ) {
        Scaffold(topBar = {
            TSAppBar(
                title = currentFolder.value.name,
                actions = {
                    IconButton(onClick = {
                        PageController.navigate("addFolder", currentFolder.value)
                    }) {
                        Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "Add Folder")
                    }
                }
            )
        }) {
            if (currentFolder.value.children.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = stringResource(id = R.string.bookmarks_empty),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                return@Scaffold
            }
            LazyColumn {
                items(currentFolder.value.children) { bookmark ->
                    when (bookmark.type) {
                        BookmarkType.Folder -> FolderItem(bookmark) {
                            currentFolder.value = bookmark
                        }
                        BookmarkType.Url -> BookmarkItem(bookmark) {
                            PageController.navigateUp()
                            TabManager.currentTab.value?.loadUrl(bookmark.url)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderItem(bookmark: Bookmark, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
        onClick()
    }) {
        Box(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                modifier = Modifier.size(32.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = bookmark.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (bookmark.children.isEmpty()) stringResource(id = R.string.empty) else stringResource(
                    id = R.string.bookmark_count,
                    bookmark.children.size
                ),
                style = MaterialTheme.typography.caption
            )
        }
        IconButton(onClick = { PageController.navigate("editBookmark", bookmark) }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
            )
        }
    }
}

@Composable
fun BookmarkItem(bookmark: Bookmark, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
        onClick()
    }) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            val icon = IconMap[bookmark.url]?.asImageBitmap()
            if (icon == null) {
                Icon(
                    imageVector = Icons.Default.NetworkCell,
                    contentDescription = "Folder",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(32.dp),
                )
            } else {
                Image(
                    bitmap = icon,
                    contentDescription = bookmark.url,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = bookmark.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = bookmark.url, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)
        }
        IconButton(onClick = { PageController.navigate("editBookmark", bookmark) }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
            )
        }
    }
}