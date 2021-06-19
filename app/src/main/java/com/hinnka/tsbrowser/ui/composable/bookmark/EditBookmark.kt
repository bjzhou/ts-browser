package com.hinnka.tsbrowser.ui.composable.bookmark

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.BookmarkType
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import com.hinnka.tsbrowser.ui.composable.widget.TSTextField
import com.hinnka.tsbrowser.ui.composable.widget.page.PageController
import com.hinnka.tsbrowser.ui.theme.DangerColor

@Composable
fun EditBookmark(bookmark: Bookmark) {
    val title = remember {
        mutableStateOf(TextFieldValue(text = bookmark.name))
    }
    val url = remember {
        mutableStateOf(TextFieldValue(text = bookmark.url))
    }
    val selectedFolder = remember {
        mutableStateOf(bookmark.parent ?: Bookmark.root)
    }
    Scaffold(topBar = {
        TSAppBar(title = stringResource(id = R.string.edit_bookmark), actions = {
            IconButton(onClick = {
                if (title.value.text.isNotBlank()) {
                    bookmark.name = title.value.text
                    bookmark.url = url.value.text
                    if (selectedFolder.value != bookmark.parent) {
                        bookmark.remove()
                        selectedFolder.value.addChild(bookmark)
                    } else {
                        bookmark.update()
                    }
                    PageController.navigateUp()
                }
            }) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Done")
            }
        })
    }) {
        Column {
            if (bookmark.type == BookmarkType.Folder) {
                TSTextField(
                    modifier = Modifier.padding(16.dp),
                    text = title,
                    placeholder = stringResource(id = R.string.title),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Folder Title"
                        )
                    }
                )
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    TSTextField(
                        text = title,
                        placeholder = stringResource(id = R.string.title),
                    )
                    TSTextField(
                        text = url,
                        placeholder = stringResource(id = R.string.url),
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.parent_folder),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Folders(Bookmark.root, selectedFolder, bookmark)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(
                    onClick = {
                        bookmark.remove()
                        PageController.navigateUp()
                    },
                    colors = buttonColors(
                        backgroundColor = DangerColor,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(id = R.string.delete))
                    }
                }
            }
        }
    }
}