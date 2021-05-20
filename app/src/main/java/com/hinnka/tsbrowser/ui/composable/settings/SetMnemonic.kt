package com.hinnka.tsbrowser.ui.composable.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.isFocused
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.ui.LocalViewModel

@Composable
fun SetMnemonic() {
    val viewModel = LocalViewModel.current
    val density = LocalDensity.current

    val currentMnemonic = remember { mutableStateOf(TextFieldValue()) }
    val newMnemonic = remember { mutableStateOf(TextFieldValue()) }
    val confirmNewMnemonic = remember { mutableStateOf(TextFieldValue()) }

    val primaryColor = MaterialTheme.colors.primary
    val currentTextColor = remember { mutableStateOf(Color.Black) }
    val newTextColor = remember { mutableStateOf(Color.Black) }
    val confirmTextColor = remember { mutableStateOf(Color.Black) }

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
                        currentTextColor.value = if (state.isFocused) primaryColor else Color.Black
                    }
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
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
                    newTextColor.value = if (state.isFocused) primaryColor else Color.Black
                }
                .padding(horizontal = 16.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
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
                    confirmTextColor.value = if (state.isFocused) primaryColor else Color.Black
                }
                .padding(horizontal = 16.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            )
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxWidth(), contentAlignment = Alignment.CenterEnd
        ) {
            Button(onClick = { /*TODO*/ }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
        Spacer(modifier = Modifier.height(with(density) { viewModel.imeHeightState.value.toDp() }))
    }
}