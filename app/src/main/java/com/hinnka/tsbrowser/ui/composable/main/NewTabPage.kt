package com.hinnka.tsbrowser.ui.composable.main

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.tap
import com.hinnka.tsbrowser.persist.AppDatabase
import com.hinnka.tsbrowser.persist.Favorite
import com.hinnka.tsbrowser.persist.IconCache
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.ui.theme.primaryLight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewTabPage() {
    val favorites = remember { mutableStateListOf<Favorite>() }
    LaunchedEffect(key1 = favorites) {
        val list = AppDatabase.instance.favoriteDao().getAll()
        favorites.clear()
        favorites.addAll(list)
    }
    Column(
        modifier = Modifier.background(MaterialTheme.colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(painter = painterResource(id = R.drawable.explorer), contentDescription = "explorer")
        NewTabTextField(modifier = Modifier.padding(32.dp))
        LazyVerticalGrid(
            cells = GridCells.Fixed(5),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(favorites) { favorite ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                    TabManager.currentTab.value?.loadUrl(favorite.url)
                }) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (favorite.iconRes != 0) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(favorite.color),
                                    shape = CircleShape
                                ), contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = favorite.iconRes),
                                contentDescription = "",
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Inside
                            )
                        }
                    } else IconCache[favorite.url]?.asImageBitmap()?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "",
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.LightGray, shape = CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.NetworkCell,
                        contentDescription = "",
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.LightGray, shape = CircleShape),
                    )
                    Text(
                        text = favorite.title,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun NewTabTextField(modifier: Modifier) {
    val viewModel = LocalViewModel.current
    Box(
        modifier = modifier.tap {
            viewModel.uiState.value = UIState.Search
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                .background(
                    color = MaterialTheme.colors.primaryLight, shape = RoundedCornerShape(20.dp),
                ),
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val color = textFieldColors().placeholderColor(true).value
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = color,
                modifier = Modifier.padding(start = 8.dp, end = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.address_bar),
                color = color,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}