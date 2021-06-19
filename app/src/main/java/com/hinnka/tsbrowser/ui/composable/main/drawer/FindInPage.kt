package com.hinnka.tsbrowser.ui.composable.main.drawer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState
import com.hinnka.tsbrowser.ui.composable.widget.TSTextField
import kotlinx.coroutines.launch

@Composable
fun FindInPage(drawerState: BottomDrawerState) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val search = remember {
        mutableStateOf(TextFieldValue())
    }
    val focusRequester = remember {
        FocusRequester()
    }
    val tab = TabManager.currentTab.value

    fun find() {
        val view = tab?.view ?: return
        view.findAllAsync(search.value.text)
    }

    fun next(forward: Boolean) {
        val view = tab?.view ?: return
        view.findNext(forward)
    }

    fun clear() {
        val view = tab?.view ?: return
        view.clearMatches()
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose {
            clear()
        }
    }

    if (AlertBottomSheet.drawerState.isClosed) {
        clear()
    }

    Row(
        Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TSTextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .padding(start = 12.dp),
            text = search,
            placeholder = stringResource(id = R.string.find_in_page),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Find in page"
                )
            },
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
            }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onValueChanged = {
                find()
            }
        )
        IconButton(onClick = { next(false) }) {
            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Up")
        }
        IconButton(onClick = { next(true) }) {
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Down")
        }
        IconButton(onClick = {
            clear()
            scope.launch {
                drawerState.close()
            }
        }) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
    }
}