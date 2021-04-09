package com.hinnka.tsbrowser.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.isFocused
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.toUrl
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.theme.lightWhite

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddressBar(uiState: MutableState<UIState>) {
    TopAppBar {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(visible = uiState.value != UIState.Search) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                }
            }
            AnimatedVisibility(
                visible = true, modifier = Modifier
                    .weight(1f)
            ) {
                if (uiState.value != UIState.TabList) {
                    AddressTextField(
                        modifier = Modifier
                            .fillMaxSize(),
                        uiState = uiState
                    )
                }
            }
            AnimatedVisibility(visible = uiState.value == UIState.Main) {
                IconButton(onClick = {
                    TabManager.currentTab.value?.goHome()
                }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                }
            }
            AnimatedVisibility(visible = uiState.value == UIState.Main) {
                TabButton(uiState)
            }
            AnimatedVisibility(visible = uiState.value == UIState.TabList) {
                CloseAll()
            }
            AnimatedVisibility(visible = uiState.value == UIState.TabList) {
                NewTab(uiState)
            }
        }
    }
}

@Composable
fun AddressTextField(modifier: Modifier, uiState: MutableState<UIState>) {
    val text = remember { mutableStateOf("") }
    val url = TabManager.currentTab.observeAsState().value?.urlState?.observeAsState()?.value
    val focusManager = LocalFocusManager.current
    if (uiState.value != UIState.Search) {
        focusManager.clearFocus()
    }
    val context = LocalContext.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colors.lightWhite, shape = RoundedCornerShape(20.dp),
                ),
        )
        TextField(
            value = text.value,
            placeholder = { Text(text = url ?: stringResource(id = R.string.address_bar)) },
            colors = textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = Color.Black,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        uiState.value = UIState.Search
                    } else {
                        uiState.value = UIState.Main
                    }
                },
            leadingIcon = when {
                uiState.value == UIState.Search -> {
                    { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") }
                }
                url?.startsWith("https") == true -> {
                    {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Https",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colors.secondary
                        )
                    }
                }
                else -> {
                    {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            modifier = Modifier.size(20.dp),
                            tint = Color.LightGray
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                focusManager.clearFocus()
                if (text.value.isBlank()) {
                    text.value = ""
                    return@KeyboardActions
                }
                if (text.value == "900902") {
                    context.startActivity(Intent(context, SecretActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    })
                    text.value = ""
                    return@KeyboardActions
                }
                TabManager.currentTab.value?.loadUrl(text.value.toUrl())
                text.value = ""
            }),
            singleLine = true,
            onValueChange = {
                text.value = it
            },
        )
    }
}

@Composable
fun TabButton(uiState: MutableState<UIState>) {
    val tabs = TabManager.tabs
    IconButton(onClick = {
        uiState.value = UIState.TabList
    }) {
        Box(
            modifier = Modifier
                .border(
                    1.dp,
                    MaterialTheme.colors.onPrimary,
                    RoundedCornerShape(4.dp)
                )
                .size(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = tabs.size.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center,
            )
        }

    }
}

@Composable
fun CloseAll() {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clickable {
                TabManager.removeAll()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Default.Close, contentDescription = "Close all")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = stringResource(id = R.string.closeAll))
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun NewTab(uiState: MutableState<UIState>) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
            .clickable {
                TabManager
                    .newTab(context)
                    .apply {
                        goHome()
                        active()
                    }
                uiState.value = UIState.Main
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Default.Add, contentDescription = "New tab")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = stringResource(id = R.string.newtab))
        Spacer(modifier = Modifier.width(8.dp))
    }
}