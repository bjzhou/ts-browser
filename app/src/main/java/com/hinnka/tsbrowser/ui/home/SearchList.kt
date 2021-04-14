package com.hinnka.tsbrowser.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.isUrl
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.viewmodel.LocalViewModel

@Composable
fun SearchList() {
    val viewModel = LocalViewModel.current
    val context = LocalContext.current
    val list = viewModel.searchList
    val tab = TabManager.currentTab.value


    viewModel.loadSearchHistory()

    LazyColumn {
        tab?.let { tab ->
            val title = tab.titleState.value
            val url = tab.urlState.value
            item {
                Row {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = url,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = {
                        viewModel.share(url, title)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = { viewModel.editInAddressBar(url) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = { viewModel.copy(url) }) {
                        Icon(
                            imageVector = Icons.Default.CopyAll,
                            contentDescription = "Copy",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(id = R.string.search_history),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
        }
        items(list.size) { index ->
            val item = list[index]
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .clickable {
                        viewModel.onGo(item.query, context)
                        viewModel.uiState.value = UIState.Main
                    }, verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.query.isUrl()) {
                    item.iconBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = item.query,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(16.dp),
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = item.query,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(16.dp),
                        tint = Color.LightGray,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = item.query,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(16.dp),
                        tint = Color.LightGray,
                    )
                }
                Text(
                    text = item.title ?: item.query,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = {
                    viewModel.delete(item)
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = item.query,
                        modifier = Modifier.size(20.dp),
                        tint = Color.LightGray,
                    )
                }
            }
        }
    }
}