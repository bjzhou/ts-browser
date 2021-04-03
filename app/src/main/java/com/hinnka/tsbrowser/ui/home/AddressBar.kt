package com.hinnka.tsbrowser.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.theme.lightWhite

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddressBar(searchState: MutableState<Boolean>) {
    TopAppBar {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(visible = !searchState.value) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                }
            }
            AddressTextField(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .onFocusChanged { state ->
                        println("focus:: $state")
                        searchState.value = state.isFocused
                    },
                searchState = searchState
            )
            AnimatedVisibility(visible = !searchState.value) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                }
            }
            AnimatedVisibility(visible = !searchState.value) {
                TabButton()
            }
        }
    }
}

@Composable
fun AddressTextField(modifier: Modifier, searchState: MutableState<Boolean>) {
    val text = remember { mutableStateOf("") }
    val url = TabManager.currentTab.observeAsState().value?.urlState?.observeAsState()?.value
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(
                    color = lightWhite, shape = RoundedCornerShape(20.dp),
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
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = if (searchState.value) {
                { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                focusManager.clearFocus()
            }),
            singleLine = true,
            onValueChange = {
                text.value = it
            },
        )
    }
}

@Composable
fun TabButton() {
    val context = LocalContext.current
    val tabSize = TabManager.tabSize.observeAsState()
    IconButton(onClick = {
        if (tabSize.value == 1) {
            TabManager.newTab(context).apply {
                view.post {
                    view.loadUrl("https://www.baidu.com")
                }
            }
        } else {
            for (tab in TabManager.tabs) {
                if (!tab.isActive) {
                    tab.active()
                    return@IconButton
                }
            }
        }
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
                text = tabSize.value.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center,
            )
        }

    }
}