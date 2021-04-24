package com.hinnka.tsbrowser.ui.composable.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ui.base.PageController
import com.hinnka.tsbrowser.ui.base.statusBarHeight

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ColumnScope.TSDrawer() {
    val secondary = MaterialTheme.colors.secondary
    Column(
        modifier = Modifier
            .height(185.dp + statusBarHeight())
            .fillMaxWidth()
            .background(secondary)
            .padding(start = 16.dp, top = statusBarHeight() + 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.ic_security),
                contentDescription = "Security"
            )
            Column(modifier = Modifier.padding(start = 32.dp)) {
                Text(
                    text = stringResource(id = R.string.privacy_protect),
                    color = MaterialTheme.colors.onSecondary
                )
                Text(
                    text = stringResource(id = R.string.days, 101),
                    color = MaterialTheme.colors.onSecondary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(id = R.string.privacy_protect),
            color = MaterialTheme.colors.onSecondary
        )
        Text(
            text = stringResource(id = R.string.days, 101),
            color = MaterialTheme.colors.onSecondary
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    ListItem(
        modifier = Modifier.clickable { },
        icon = { Icon(imageVector = Icons.Outlined.Bookmarks, contentDescription = "Bookmarks") },
    ) {
        Text(text = stringResource(id = R.string.bookmark))
    }
    ListItem(
        modifier = Modifier.clickable { },
        icon = { Icon(imageVector = Icons.Outlined.History, contentDescription = "History") },
    ) {
        Text(text = stringResource(id = R.string.history))
    }
    ListItem(
        modifier = Modifier.clickable {
            PageController.navigate("downloads")
        },
        icon = { Icon(imageVector = Icons.Outlined.Download, contentDescription = "Downloads") },
    ) {
        Text(text = stringResource(id = R.string.downloads))
    }
}