package com.hinnka.tsbrowser.ui.composable.main.favorite

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.Favorite
import com.hinnka.tsbrowser.persist.Favorites
import com.hinnka.tsbrowser.persist.IconMap
import com.hinnka.tsbrowser.tab.TabManager


@Composable
fun FavoriteItem(favorite: Favorite, onUpdate: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val showPopup = remember { mutableStateOf(false) }
    val defaultFav = Favorites.default.firstOrNull { it.url == favorite.url }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    showPopup.value = true
                },
                onTap = {
                    TabManager.currentTab.value?.loadUrl(favorite.url)
                }
            )
        }) {
        Spacer(modifier = Modifier.height(8.dp))
        val resourceValid = try {
            defaultFav?.iconRes?.let { context.resources.getResourceName(it) != null } ?: false
        } catch (e: Exception) {
            false
        }
        if (defaultFav != null && resourceValid) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(defaultFav.color),
                        shape = CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = defaultFav.iconRes),
                    contentDescription = "",
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.Inside
                )
            }
        } else IconMap[favorite.url]?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "",
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent, shape = CircleShape),
                contentScale = ContentScale.Crop
            )
        } ?: Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colors.primary
        )
        Text(
            text = favorite.title,
            modifier = Modifier.padding(vertical = 8.dp),
            fontSize = 12.sp
        )
    }
    DropdownMenu(
        expanded = showPopup.value,
        offset = DpOffset.Zero,
        onDismissRequest = {
            showPopup.value = false
        },
    ) {
        DropdownMenuItem(onClick = {
            showPopup.value = false
            onUpdate()
        }) {
            Text(text = stringResource(id = R.string.edit))
        }
        DropdownMenuItem(onClick = {
            showPopup.value = false
            onDelete()
        }) {
            Text(text = stringResource(id = R.string.delete))
        }
    }
}