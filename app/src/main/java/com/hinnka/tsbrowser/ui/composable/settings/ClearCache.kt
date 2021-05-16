package com.hinnka.tsbrowser.ui.composable.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ui.LocalViewModel
import kotlinx.coroutines.launch

@Composable
fun ClearCache(block: () -> Unit) {
    val clearCookies = remember { mutableStateOf(true) }
    val clearSiteData = remember { mutableStateOf(true) }
    val clearHistory = remember { mutableStateOf(true) }
    val clearSearch = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val viewModel = LocalViewModel.current

    Column {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.clear_cookies), modifier = Modifier.weight(1f))
            Checkbox(checked = clearCookies.value, onCheckedChange = {
                clearCookies.value = it
            })
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.clear_site_data), modifier = Modifier.weight(1f))
            Checkbox(checked = clearSiteData.value, onCheckedChange = {
                clearSiteData.value = it
            })
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.clear_browse_history), modifier = Modifier.weight(1f))
            Checkbox(checked = clearHistory.value, onCheckedChange = {
                clearHistory.value = it
            })
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.clear_search_history), modifier = Modifier.weight(1f))
            Checkbox(checked = clearSearch.value, onCheckedChange = {
                clearSearch.value = it
            })
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                scope.launch {
                    viewModel.clearData(clearCookies.value, clearSiteData.value, clearHistory.value, clearSearch.value)
                    block()
                }
            }) {
                Text(text = stringResource(id = R.string.clear_browse_data))
            }
        }
    }
}