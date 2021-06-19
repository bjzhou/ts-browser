package com.hinnka.tsbrowser.ui.composable.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleEventObserver
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.NameValue
import com.hinnka.tsbrowser.persist.SettingOptions
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import com.hinnka.tsbrowser.ui.composable.widget.TSBottomDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsPage() {
    val viewModel = LocalViewModel.current
    val scope = rememberCoroutineScope()
    val checkedItem = remember { mutableStateOf(NameValue("", "")) }
    val context = LocalContext.current
    val isDefaultBrowser = remember { mutableStateOf(viewModel.isDefaultBrowser) }
    val state = remember { BottomDrawerState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            isDefaultBrowser.value = viewModel.isDefaultBrowser
            viewModel.updateDefaultBrowserBadgeState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    TSBottomDrawer(drawerState = state) {
        Column(Modifier.background(MaterialTheme.colors.surface)) {
            TSAppBar(title = stringResource(id = R.string.settings))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = R.string.general),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colors.primary,
                    fontSize = 13.sp
                )
                ListItem(
                    modifier = Modifier.clickable {
                        viewModel.openDefaultBrowserSetting(context)
                    },
                    secondaryText = { Text(text = stringResource(id = if (isDefaultBrowser.value) R.string.on else R.string.off)) },
                    text = { Text(text = stringResource(id = R.string.set_default_browser)) },
                    trailing = {
                        Row {
                            if (viewModel.canShowDefaultBrowserBadgeState.value) {
                                Box(Modifier.padding(8.dp)) {
                                    Spacer(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.Red, CircleShape)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = ""
                            )
                        }
                    }
                )
                ListItem(
                    modifier = Modifier.clickable {
                        checkedItem.value = Settings.searchEngine
                        state.open {
                            SettingOption(
                                options = SettingOptions.searchEngine,
                                checked = checkedItem
                            ) { newValue ->
                                checkedItem.value = newValue
                                Settings.searchEngine = newValue
                                scope.launch {
                                    state.close()
                                }
                            }
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
                        checkedItem.value = Settings.userAgent
                        state.open {
                            SettingOption(
                                options = SettingOptions.userAgent,
                                checked = checkedItem
                            ) { newValue ->
                                checkedItem.value = newValue
                                Settings.userAgent = newValue
                                scope.launch {
                                    state.close()
                                }
                            }
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
                    modifier = Modifier.clickable {
                        state.open {
                            SetMnemonic {
                                scope.launch {
                                    state.close()
                                }
                            }
                        }
                    },
                    secondaryText = { Text(text = stringResource(id = R.string.mnemonic_hint)) },
                    text = {
                        Text(
                            text = stringResource(
                                id = if (Settings.mnemonicState.value == null) {
                                    R.string.set_mnemonic
                                } else R.string.update_mnemonic
                            )
                        )
                    },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable {
                        state.open {
                            ClearCache {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.clear_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                scope.launch {
                                    state.close()
                                }
                            }
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
                            },
                            enabled = !App.isSecretMode
                        )
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
                        Switch(
                            checked = Settings.dntState.value,
                            onCheckedChange = {
                                Settings.dnt = it
                            },
                            enabled = !App.isSecretMode
                        )
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
                                "https://play.google.com/store/apps/details?id=com.hinnka.tsbrowser"
                            )
                            setPackage("com.android.vending")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                        }
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