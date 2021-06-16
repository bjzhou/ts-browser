package com.hinnka.tsbrowser.ui.composable.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.tap
import com.hinnka.tsbrowser.ui.composable.settings.SetMnemonic
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import kotlinx.coroutines.launch

@Composable
fun SecretWelcome() {
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TSAppBar(title = stringResource(id = R.string.secret_mode), showBack = false)
        },
        modifier = Modifier.fillMaxSize().tap {  }
    ) {
        Column(modifier = Modifier.padding(16.dp).background(MaterialTheme.colors.surface)) {
            Text(
                text = stringResource(R.string.what_is_secret_mode),
                style = MaterialTheme.typography.h6
            )
            Text(
                text = stringResource(R.string.what_is_secret_mode_desc),
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Text(text = stringResource(R.string.how_to_use), style = MaterialTheme.typography.h6)
            Text(
                text = stringResource(R.string.how_to_use_desc),
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.welcome_secret),
                    contentDescription = "welcome",
                )
            }
            Button(
                onClick = {
                    AlertBottomSheet.open {
                        SetMnemonic {
                            scope.launch {
                                AlertBottomSheet.close()
                            }
                        }
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(text = stringResource(R.string.set_mnemonic))
            }
        }
    }
}