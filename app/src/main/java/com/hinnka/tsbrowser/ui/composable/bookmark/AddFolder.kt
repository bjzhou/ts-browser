package com.hinnka.tsbrowser.ui.composable.bookmark

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.BookmarkType
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import com.hinnka.tsbrowser.ui.composable.widget.TSTextField
import com.hinnka.tsbrowser.ui.composable.widget.page.PageController

@Composable
fun AddFolder(parentBookmark: Bookmark) {
    val title = remember {
        mutableStateOf(TextFieldValue())
    }
    val selectedFolder = remember {
        mutableStateOf(parentBookmark)
    }
    Scaffold(topBar = {
        TSAppBar(title = stringResource(id = R.string.add_folder), actions = {
            IconButton(onClick = {
                if (title.value.text.isNotBlank()) {
                    selectedFolder.value.addChild(
                        Bookmark(
                            name = title.value.text,
                            type = BookmarkType.Folder
                        )
                    )
                    PageController.navigateUp()
                }
            }) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Done")
            }
        })
    }) {
        Column {
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
            Text(text = stringResource(id = R.string.parent_folder), modifier = Modifier.padding(horizontal = 16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Folders(Bookmark.root, selectedFolder)
            }
        }
    }
}
