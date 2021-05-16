package com.hinnka.tsbrowser.ui.composable.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.NameValue
import com.hinnka.tsbrowser.persist.SettingOptions
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.ui.composable.wiget.BottomDrawerState
import com.hinnka.tsbrowser.ui.composable.wiget.TSAppBar
import com.hinnka.tsbrowser.ui.composable.wiget.TSBottomDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsPage() {
    val state = remember { BottomDrawerState() }
    val scope = rememberCoroutineScope()
    val sheetItems = remember { mutableStateOf(listOf<NameValue>()) }
    val checkedItem = remember { mutableStateOf(NameValue("", "")) }
    val showClearDataSheet = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val itemHeight = with(LocalDensity.current) { 56.dp.toPx() }

    TSBottomDrawer(
        drawerContent = {
            if (showClearDataSheet.value) {
                ClearCache {
                    Toast.makeText(context, context.getString(R.string.clear_success), Toast.LENGTH_SHORT).show()
                    scope.launch {
                        state.close()
                    }
                }
            } else {
                SettingOption(options = sheetItems.value, checked = checkedItem) { newValue ->
                    checkedItem.value = newValue
                    when (sheetItems.value) {
                        SettingOptions.searchEngine -> {
                            Settings.searchEngine = newValue
                        }
                        SettingOptions.userAgent -> {
                            Settings.userAgent = newValue
                        }
                    }
                    scope.launch {
                        state.close()
                    }
                }
            }
        },
        drawerState = state,
    ) {
        Column(Modifier.background(MaterialTheme.colors.surface)) {
            TSAppBar(title = stringResource(id = R.string.settings))
            Column(modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(id = R.string.general),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colors.primary,
                    fontSize = 13.sp
                )
                ListItem(
                    modifier = Modifier.clickable {
                        showClearDataSheet.value = false
                        sheetItems.value = SettingOptions.searchEngine
                        checkedItem.value = Settings.searchEngine
                        state.drawerHeight = sheetItems.value.size * itemHeight
                        scope.launch {
                            state.open()
                        }
                    },
                    secondaryText = { Text(text = Settings.searchEngineState.value.name) },
                    text = { Text(text = stringResource(id = R.string.search_engine)) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable {
                        showClearDataSheet.value = true
                        state.drawerHeight = 5 * itemHeight
                        scope.launch {
                            state.open()
                        }
                    },
                    secondaryText = { Text(text = stringResource(id = R.string.clear_browse_data_hint)) },
                    text = { Text(text = stringResource(id = R.string.clear_browse_data)) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable {
                        showClearDataSheet.value = false
                        sheetItems.value = SettingOptions.userAgent
                        checkedItem.value = Settings.userAgent
                        state.drawerHeight = sheetItems.value.size * itemHeight
                        scope.launch {
                            state.open()
                        }
                    },
                    secondaryText = { Text(text = Settings.userAgentState.value.name) },
                    text = { Text(text = stringResource(id = R.string.user_agent)) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                )
                ListItem(
                    secondaryText = {
                        Text(
                            text = if (Settings.adblockState.value) {
                                stringResource(id = R.string.on)
                            } else {
                                stringResource(id = R.string.off)
                            }
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.adblock)) },
                    trailing = {
                        Switch(checked = Settings.adblockState.value, onCheckedChange = {
                            Settings.adblock = it
                        })
                    }
                )
                Text(
                    text = stringResource(id = R.string.privacy),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colors.primary,
                    fontSize = 13.sp
                )
                ListItem(
                    secondaryText = {
                        Text(
                            text = if (Settings.acceptThirdPartyCookiesState.value) {
                                stringResource(id = R.string.on)
                            } else {
                                stringResource(id = R.string.off)
                            }
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.accept_thirdparty_cookie)) },
                    trailing = {
                        Switch(
                            checked = Settings.acceptThirdPartyCookiesState.value,
                            onCheckedChange = {
                                Settings.acceptThirdPartyCookies = it
                            })
                    }
                )
                ListItem(
                    secondaryText = {
                        Text(
                            text = if (Settings.dntState.value) {
                                stringResource(id = R.string.on)
                            } else {
                                stringResource(id = R.string.off)
                            }
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.dnt)) },
                    trailing = {
                        Switch(checked = Settings.dntState.value, onCheckedChange = {
                            Settings.dnt = it
                        })
                    }
                )
                Text(
                    text = stringResource(id = R.string.about),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colors.primary,
                    fontSize = 13.sp
                )
                ListItem(
                    modifier = Modifier.clickable { },
                    secondaryText = { Text(text = "1.0.0") },
                    text = { Text(text = stringResource(id = R.string.version)) },
                )
                ListItem(
                    modifier = Modifier.clickable { },
                    text = { Text(text = stringResource(id = R.string.feedback)) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(
                                "https://play.google.com/store/apps/details?id=com.hinnka.tsbrowser")
                            setPackage("com.android.vending")
                        }
                        context.startActivity(intent)
                    },
                    text = { Text(text = stringResource(id = R.string.update)) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable { },
                    text = { Text(text = stringResource(id = R.string.privacy_policy)) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable { },
                    text = { Text(text = stringResource(id = R.string.terms_of_service)) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                )
            }
        }
    }
}