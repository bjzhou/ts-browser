package com.hinnka.tsbrowser.ui.composable.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.persist.NameValue

@Composable
fun SettingOption(options: List<NameValue>, checked: State<NameValue>, block: (NameValue) -> Unit) {
    LazyColumn(modifier = Modifier
        .height((56 * options.size).dp.coerceAtLeast(168.dp))
        .fillMaxWidth()
        .background(MaterialTheme.colors.surface)) {
        items(options) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(56.dp)
                    .clickable {
                        block(item)
                    }
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = item.name, modifier = Modifier.weight(1f))
                if (checked.value.name == item.name) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Check")
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}