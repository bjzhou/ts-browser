package com.hinnka.tsbrowser.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.tap
import com.hinnka.tsbrowser.tab.Tab
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabList(uiState: MutableState<UIState>) {
    val state = rememberLazyListState()
    val tabs = TabManager.tabs
    LazyVerticalGrid(
        cells = GridCells.Fixed(2),
        state = state,
        contentPadding = PaddingValues(8.dp)
    ) {
        items(tabs.size) {
            Card(
                elevation = 2.dp, modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .padding(8.dp)
            ) {
                TabItem(tab = tabs[it], uiState)
            }
        }
    }
}

@Composable
fun TabItem(tab: Tab, uiState: MutableState<UIState>) {
    val icon = tab.iconState.observeAsState()
    val title = tab.titleState.observeAsState()
    val preview = tab.previewState.observeAsState()
    Column(modifier = Modifier
        .tap {
            tab.active()
            uiState.value = UIState.Main
        }
        .border(
            width = if (tab.isActive) 2.dp else 0.dp,
            color = MaterialTheme.colors.secondary,
            shape = RoundedCornerShape(4.dp)
        )) {
        Row(modifier = Modifier.height(28.dp), verticalAlignment = Alignment.CenterVertically) {
            icon.value?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(22.dp)
                        .padding(3.dp)
                )
            }
            Text(
                text = title.value ?: stringResource(id = R.string.untiled),
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        TabManager.remove(tab)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(22.dp)
                        .padding(3.dp)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(0.5f.dp)
                .fillMaxWidth()
                .background(Color.LightGray)
        )
        preview.value?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = tab.urlState.value,
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.FillWidth
            )
        }
    }
}