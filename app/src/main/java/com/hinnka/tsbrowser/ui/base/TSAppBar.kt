package com.hinnka.tsbrowser.ui.base

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TSAppBar(
    title: String,
    showBack: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(modifier = Modifier.height(56.dp), verticalAlignment = Alignment.CenterVertically) {
        if (showBack) {
            IconButton(onClick = { PageController.navigateUp() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(text = title, style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
        actions()
    }
}