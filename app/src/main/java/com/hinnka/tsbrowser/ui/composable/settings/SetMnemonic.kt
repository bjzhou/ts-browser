package com.hinnka.tsbrowser.ui.composable.settings

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.md5
import com.hinnka.tsbrowser.persist.Settings

@Composable
fun SetMnemonic(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val contentColor = LocalContentColor.current

    val currentMnemonic = remember { mutableStateOf(TextFieldValue()) }
    val newMnemonic = remember { mutableStateOf(TextFieldValue()) }
    val confirmNewMnemonic = remember { mutableStateOf(TextFieldValue()) }

    val primaryColor = MaterialTheme.colors.primary
    val currentTextColor = remember { mutableStateOf(contentColor) }
    val newTextColor = remember { mutableStateOf(contentColor) }
    val confirmTextColor = remember { mutableStateOf(contentColor) }

    val buttonEnable = if (Settings.mnemonic == null) {
        true
    } else {
        currentMnemonic.value.text.isNotBlank()
    } && newMnemonic.value.text.isNotBlank() && confirmNewMnemonic.value.text.isNotBlank()

    fun updateMnemonic() {
        Settings.mnemonic?.let {
            if (currentMnemonic.value.text.md5() != it) {
                Toast.makeText(context, R.string.current_mnemonic_wrong, Toast.LENGTH_SHORT).show()
                return
            }
        }
        if (newMnemonic.value.text != confirmNewMnemonic.value.text) {
            Toast.makeText(context, R.string.mnemonic_different, Toast.LENGTH_SHORT).show()
            return
        }
        Settings.mnemonic = newMnemonic.value.text.md5()
        Toast.makeText(context, R.string.mnemonic_update_success, Toast.LENGTH_SHORT).show()
        currentMnemonic.value = TextFieldValue()
        newMnemonic.value = TextFieldValue()
        confirmNewMnemonic.value = TextFieldValue()
        onDismiss()
    }


    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (Settings.mnemonic == null)
                    stringResource(id = R.string.set_mnemonic)
                else stringResource(id = R.string.update_mnemonic),
                fontWeight = FontWeight.Bold
            )
        }
        if (Settings.mnemonic != null) {
            Text(
                text = stringResource(id = R.string.current_mnemonic),
                color = currentTextColor.value,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            TextField(
                value = currentMnemonic.value,
                onValueChange = {
                    currentMnemonic.value = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        currentTextColor.value = if (state.isFocused) primaryColor else contentColor
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
        }
        Text(
            text = stringResource(id = R.string.new_mnemonic),
            color = newTextColor.value,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        TextField(
            value = newMnemonic.value,
            onValueChange = {
                newMnemonic.value = it
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
            text = stringResource(id = R.string.confirm_new_mnemonic),
            color = confirmTextColor.value,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        TextField(
            value = confirmNewMnemonic.value,
            onValueChange = {
                confirmNewMnemonic.value = it
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
            Button(onClick = { updateMnemonic() }, enabled = buttonEnable) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    }
}