package com.hinnka.tsbrowser.ui.composable.history

import android.webkit.WebStorage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.isSameDay
import com.hinnka.tsbrowser.ext.toDateString
import com.hinnka.tsbrowser.persist.AppDatabase
import com.hinnka.tsbrowser.persist.History
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.composable.widget.page.PageController
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import com.hinnka.tsbrowser.ui.composable.widget.TSTextField
import com.hinnka.tsbrowser.persist.IconMap
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HistoryPage() {
    val scope = rememberCoroutineScope()
    val searchText = remember { mutableStateOf(TextFieldValue()) }
    val focusState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val pager = remember {
        Pager(
            PagingConfig(pageSize = 20)
        ) {
            if (searchText.value.text.isNotBlank()) {
                AppDatabase.instance.historyDao().search("%${searchText.value.text.trim()}%")
            } else {
                AppDatabase.instance.historyDao().getAll()
            }
        }
    }
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    Scaffold(topBar = {
        TSAppBar(title = stringResource(id = R.string.history), actions = {
            IconButton(onClick = {
                AlertBottomSheet.Builder(context).apply {
                    setMessage(context.getString(R.string.clear_all_history))
                    setPositiveButton(R.string.clear) {
                        scope.launch {
                            WebStorage.getInstance().deleteAllData()
                            AppDatabase.instance.historyDao().clear()
                            lazyPagingItems.refresh()
                        }
                    }
                    setNegativeButton(android.R.string.cancel) {
                    }
                }.show()
            }) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Clear All",
                )
            }
        })
    }) {
        LazyColumn {
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TSTextField(
                        text = searchText,
                        modifier = Modifier.weight(1f),
                        placeholder = stringResource(id = R.string.search_history),
                        onFocusChanged = {
                            focusState.value = it.isFocused
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        onValueChanged = {
                            lazyPagingItems.refresh()
                        }
                    )
                    AnimatedVisibility(visible = focusState.value) {
                        IconButton(
                            modifier = Modifier.widthIn(56.dp, 72.dp),
                            onClick = {
                                searchText.value = TextFieldValue()
                                focusState.value = false
                                focusManager.clearFocus()
                                lazyPagingItems.refresh()
                            }
                        ) {
                            Text(text = stringResource(id = R.string.action_cancel))
                        }
                    }
                }
            }

            itemsIndexed(lazyPagingItems) { index, item ->
                item?.let {
                    Column {
                        DateItemOrNull(lazyPagingItems, index)
                        HistoryItem(
                            history = item,
                            onClick = {
                                PageController.navigateUp()
                                TabManager.currentTab.value?.loadUrl(item.url)
                            },
                            onDeleteItem = {
                                scope.launch {
                                    AppDatabase.instance.historyDao().delete(item)
                                    lazyPagingItems.refresh()
                                }
                            },
                        )
                    }
                }
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun DateItemOrNull(lazyPagingItems: LazyPagingItems<History>, index: Int) {
    val item = lazyPagingItems.getAsState(index).value ?: return
    val canShow = if (index == 0) {
        true
    } else {
        val prevItem = lazyPagingItems.getAsState(index - 1).value ?: return
        !(prevItem.date isSameDay item.date)
    }
    if (canShow) {
        Text(
            text = item.date.toDateString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .wrapContentHeight(Alignment.CenterVertically)
        )
    }
}

@Composable
fun HistoryItem(history: History, onClick: () -> Unit, onDeleteItem: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
        onClick()
    }) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            val icon = IconMap[history.url]?.asImageBitmap()
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
                    contentDescription = history.url,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = history.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = history.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.caption
            )
        }
        IconButton(onClick = {
            onDeleteItem()
        }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
            )
        }
    }
}