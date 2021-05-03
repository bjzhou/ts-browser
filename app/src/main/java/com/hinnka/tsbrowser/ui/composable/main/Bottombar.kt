package com.hinnka.tsbrowser.ui.composable.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.isFocused
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.composable.wiget.TSTextField
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.viewmodel.LocalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun BottomBar(drawerState: BottomDrawerState) {
    val viewModel = LocalViewModel.current
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.graphicsLayer {
        translationY = -viewModel.imeHeightState.value
    }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(56.dp)
                .background(Color.White)
                .padding(start = if (uiState.value == UIState.Search) 8.dp else 0.dp),
        ) {
            AnimatedVisibility(visible = uiState.value == UIState.Main) {
                HomeButton()
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
            AnimatedVisibility(visible = true, modifier = Modifier.weight(1f)) {
                if (uiState.value != UIState.TabList) {
                    AddressTextField(
                        modifier = Modifier.fillMaxSize(),
                        uiState = uiState
                    )
                }
            }
            AnimatedVisibility(visible = uiState.value == UIState.Search) {
                CancelButton(uiState)
            }
            AnimatedVisibility(visible = uiState.value == UIState.Main) {
                RefreshButton()
            }
            AnimatedVisibility(visible = uiState.value != UIState.Search) {
                IconButton(onClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                }
            }
        }
    }
}

@Composable
fun AddressTextField(modifier: Modifier, uiState: MutableState<UIState>) {
    val viewModel = LocalViewModel.current
    val text = viewModel.addressText
    val tab = TabManager.currentTab.value
    val url = tab?.urlState?.value
    val title = tab?.titleState?.value
    val icon = tab?.iconState?.value
    val focusManager = LocalFocusManager.current
    if (uiState.value != UIState.Search) {
        focusManager.clearFocus()
    }
    val context = LocalContext.current


    fun onGo() {
        focusManager.clearFocus()
        viewModel.onGo(text.value.text, context)
        text.value = TextFieldValue()
        uiState.value = UIState.Main
    }

    TSTextField(
        modifier = modifier,
        text = text,
        placeholder = if (title.isNullOrBlank()) url
            ?: stringResource(id = R.string.address_bar) else title,
        onEnter = { onGo() },
        onFocusChanged = { state ->
            if (state.isFocused) {
                uiState.value = UIState.Search
            }
        },
        leadingIcon = when (uiState.value) {
            UIState.Search -> {
                { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") }
            }
            else -> {
                {
                    icon?.asImageBitmap()?.let {
                        Image(
                            bitmap = it,
                            contentDescription = url,
                            modifier = Modifier.size(20.dp),
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        modifier = Modifier.size(20.dp),
                        tint = Color.LightGray
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go)
    )
}

@Composable
fun TabButton(uiState: MutableState<UIState>) {
    val tabs = TabManager.tabs
    IconButton(onClick = {
        TabManager.currentTab.value?.view?.generatePreview()
        uiState.value = UIState.TabList
    }) {
        Box(
            modifier = Modifier
                .border(
                    1.5.dp,
                    LocalContentColor.current,
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

@Composable
fun CancelButton(uiState: MutableState<UIState>) {
    val viewModel = LocalViewModel.current
    val scope = rememberCoroutineScope()
    IconButton(
        modifier = Modifier.widthIn(56.dp, 72.dp),
        onClick = {
            uiState.value = UIState.Main
            scope.launch {
                //FIXME why need to delay
                delay(50)
                viewModel.addressText.value = TextFieldValue()
            }
        }
    ) {
        Text(text = stringResource(id = R.string.action_cancel))
    }

}



@Composable
fun HomeButton() {
    IconButton(onClick = {
        TabManager.currentTab.value?.goHome()
    }) {
        Icon(imageVector = Icons.Outlined.Home, contentDescription = "Home")
    }
}

@Composable
fun RefreshButton() {
    val tab by TabManager.currentTab
    val progress = tab?.progressState?.value
    IconButton(
        onClick = {
            if (progress == 1f) {
                tab?.view?.reload()
            } else {
                tab?.view?.stopLoading()
            }
        },
        enabled = tab != null
    ) {
        Icon(
            imageVector = if (progress == 1f) Icons.Outlined.Refresh else Icons.Outlined.Close,
            contentDescription = "Refresh"
        )
    }
}