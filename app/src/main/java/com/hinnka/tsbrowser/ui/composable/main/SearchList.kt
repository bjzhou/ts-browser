package com.hinnka.tsbrowser.ui.composable.main

import android.webkit.URLUtil
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.isUrl
import com.hinnka.tsbrowser.tab.Tab
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.ui.AppViewModel
import com.hinnka.tsbrowser.ui.LocalViewModel

@Composable
fun SearchList() {
    val viewModel = LocalViewModel.current
    val context = LocalContext.current
    val list = viewModel.searchList
    val tab = TabManager.currentTab.value
    val density = LocalDensity.current

    val clipboard = LocalClipboardManager.current
    val clipboardText = clipboard.getText()

    viewModel.loadSearchHistory()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = with(density) { viewModel.imeHeightState.value.toDp() })
        .background(MaterialTheme.colors.surface),
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
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
                    Spacer(modifier = Modifier.width(16.dp))
                    if (item.query.isUrl()) {
                        item.iconBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = item.query,
                                modifier = Modifier.size(32.dp),
                            )
                        } ?: Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = item.query,
                            modifier = Modifier.size(32.dp),
                            tint = Color.LightGray,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = item.query,
                            modifier = Modifier.size(32.dp),
                            tint = Color.LightGray,
                        )
                    }
                    Text(
                        text = item.title ?: item.query,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
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
        if (!clipboardText.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .clickable {
                        viewModel.onGo(clipboardText.text, context)
                        viewModel.uiState.value = UIState.Main
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .size(32.dp),
                    tint = Color.LightGray,
                )
                Text(
                    text = stringResource(id = R.string.click_search),
                    modifier = Modifier.padding(start = 8.dp),
                )
                Text(
                    text = " \"",
                )
                Text(
                    text = clipboardText.text,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false),
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "\"",
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
        tab?.let { tab ->
            CurrentUrl(viewModel = viewModel, tab = tab)
        }
    }
}

@Composable
fun CurrentUrl(viewModel: AppViewModel, tab: Tab) {
    val title = tab.titleState.value
    val url = tab.urlState.value
    if (!URLUtil.isNetworkUrl(url)) return
    Row(modifier = Modifier.clickable {
        viewModel.uiState.value = UIState.Main
    }) {
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