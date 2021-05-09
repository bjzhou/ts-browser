package com.hinnka.tsbrowser.ui.composable.bookmark

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.BookmarkType


@Composable
fun Folders(bookmark: Bookmark, selectedFolder: MutableState<Bookmark>, exclude: Bookmark? = null) {
    Row(
        modifier = Modifier.clickable { selectedFolder.value = bookmark },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                modifier = Modifier.size(32.dp)
            )
        }
        Text(text = bookmark.name, modifier = Modifier.weight(1f))
        if (bookmark.guid == selectedFolder.value.guid) {
            Box(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Checked",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
    val folderChildren = bookmark.children.filter { it.type == BookmarkType.Folder && it.guid != exclude?.guid }
    if (folderChildren.isNotEmpty()) {
        Column(modifier = Modifier.padding(start = 16.dp)) {
            for (folderChild in folderChildren) {
                Folders(bookmark = folderChild, selectedFolder = selectedFolder, exclude = exclude)
            }
        }
    }
}