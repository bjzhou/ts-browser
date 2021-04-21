package com.hinnka.tsbrowser.ui.composable.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ui.base.statusBarHeight

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ColumnScope.TSDrawer() {
    val secondary = MaterialTheme.colors.secondary
    Box(
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
            .background(secondary)
            .padding(start = 16.dp, top = statusBarHeight() + 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_security),
            contentDescription = "Security"
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    ListItem(
        modifier = Modifier.clickable { },
        icon = { Icon(imageVector = Icons.Outlined.Bookmarks, contentDescription = "Bookmarks") },
    ) {
        Text(text = stringResource(id = R.string.bookmark))
    }
}