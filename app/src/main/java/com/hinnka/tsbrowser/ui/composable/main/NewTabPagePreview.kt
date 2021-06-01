package com.hinnka.tsbrowser.ui.composable.main

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.host
import com.hinnka.tsbrowser.persist.AppDatabase
import com.hinnka.tsbrowser.persist.Favorite
import com.hinnka.tsbrowser.persist.IconCache
import com.hinnka.tsbrowser.ui.theme.primaryLight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewTabPagePreview() {
    val favorites = remember { mutableStateListOf<Favorite>() }

    suspend fun refresh() {
        val list = AppDatabase.instance.favoriteDao().getAll()
        favorites.clear()
        favorites.addAll(list)
    }

    LaunchedEffect(key1 = favorites) {
        refresh()
    }
    Column(
        modifier = Modifier.background(MaterialTheme.colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(painter = painterResource(id = R.drawable.explorer), modifier = Modifier.scale(0.5f), contentDescription = "explorer")
        NewTabTextFieldPreview()
        LazyVerticalGrid(
            cells = GridCells.Fixed(5),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(favorites) { favorite ->
                FavoriteItemPreview(favorite)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun FavoriteItemPreview(favorite: Favorite) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(4.dp))
        if (favorite.iconRes != 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        Color(favorite.color),
                        shape = CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = favorite.iconRes),
                    contentDescription = "",
                    modifier = Modifier.size(14.dp),
                    contentScale = ContentScale.Inside
                )
            }
        } else IconCache[favorite.url.host ?: ""]?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "",
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Transparent, shape = CircleShape),
                contentScale = ContentScale.Crop
            )
        } ?: Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colors.primary
        )
        Text(
            text = favorite.title,
            modifier = Modifier.padding(vertical = 4.dp),
            fontSize = 6.sp
        )
    }
}

@Composable
fun NewTabTextFieldPreview() {
    Box(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                .background(
                    color = MaterialTheme.colors.primaryLight, shape = RoundedCornerShape(10.dp),
                ),
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val color = textFieldColors().placeholderColor(true).value
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = color,
                modifier = Modifier.padding(start = 4.dp, end = 8.dp).scale(0.5f)
            )
            Text(
                text = stringResource(id = R.string.address_bar),
                color = color,
                fontSize = 7.sp,
                maxLines = 1
            )
        }
    }
}