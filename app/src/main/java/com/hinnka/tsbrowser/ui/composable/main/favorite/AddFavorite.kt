package com.hinnka.tsbrowser.ui.composable.main.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.AppDatabase
import com.hinnka.tsbrowser.persist.Favorite

@Composable
fun AddFavorite(orig: Favorite? = null, onDismiss: (Favorite) -> Unit) {
    val focusManager = LocalFocusManager.current
    val contentColor = LocalContentColor.current

    val titleField = remember { mutableStateOf(TextFieldValue(orig?.title ?: "")) }
    val urlField = remember { mutableStateOf(TextFieldValue(orig?.url ?: "")) }


    val primaryColor = MaterialTheme.colors.primary
    val newTextColor = remember { mutableStateOf(contentColor) }
    val confirmTextColor = remember { mutableStateOf(contentColor) }

    val buttonEnable = titleField.value.text.isNotBlank() && urlField.value.text.isNotBlank()

    fun addFavorite() {
        val favorite = orig?.let {
            it.apply {
                url = urlField.value.text
                title = titleField.value.text
            }
        } ?: Favorite(urlField.value.text, titleField.value.text)
        AppDatabase.instance.favoriteDao().insertOrUpdate(favorite)
        titleField.value = TextFieldValue()
        urlField.value = TextFieldValue()
        onDismiss(favorite)
    }


    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = if (orig == null) R.string.add else R.string.edit),
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = stringResource(id = R.string.title),
            color = newTextColor.value,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        TextField(
            value = titleField.value,
            onValueChange = {
                titleField.value = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    newTextColor.value = if (state.isFocused) primaryColor else contentColor
                }
                .padding(horizontal = 16.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.None
            )
        )
        Text(
            text = stringResource(id = R.string.url),
            color = confirmTextColor.value,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        TextField(
            value = urlField.value,
            onValueChange = {
                urlField.value = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    confirmTextColor.value = if (state.isFocused) primaryColor else contentColor
                }
                .padding(horizontal = 16.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            }),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.None
            )
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxWidth(), contentAlignment = Alignment.CenterEnd
        ) {
            Button(onClick = { addFavorite() }, enabled = buttonEnable) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    }
}