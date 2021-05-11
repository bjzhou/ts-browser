package com.hinnka.tsbrowser.ui.composable.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ui.composable.wiget.TSAppBar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsPage() {
    Scaffold(topBar = {
        TSAppBar(title = stringResource(id = R.string.settings))
    }) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(id = R.string.general),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colors.primary,
                fontSize = 13.sp
            )
            ListItem(
                modifier = Modifier.clickable {  },
                secondaryText = { Text(text = "Google") },
                text = { Text(text = stringResource(id = R.string.search_engine)) },
                trailing = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = ""
                    )
                }
            )
            ListItem(
                modifier = Modifier.clickable {  },
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
                modifier = Modifier.clickable {  },
                secondaryText = { Text(text = "Android") },
                text = { Text(text = stringResource(id = R.string.user_agent)) },
                trailing = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = ""
                    )
                }
            )
            ListItem(
                modifier = Modifier.clickable {  },
                secondaryText = { Text(text = "On") },
                text = { Text(text = stringResource(id = R.string.adblock)) },
                trailing = {
                    Switch(checked = true, onCheckedChange = {
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
                modifier = Modifier.clickable {  },
                secondaryText = { Text(text = "On") },
                text = { Text(text = stringResource(id = R.string.accept_thirdparty_cookie)) },
                trailing = {
                    Switch(checked = true, onCheckedChange = {
                    })
                }
            )
            ListItem(
                modifier = Modifier.clickable {  },
                secondaryText = { Text(text = "Off") },
                text = { Text(text = stringResource(id = R.string.dnt)) },
                trailing = {
                    Switch(checked = false, onCheckedChange = {
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
                modifier = Modifier.clickable {  },
                secondaryText = { Text(text = "1.0.0") },
                text = { Text(text = stringResource(id = R.string.version)) },
            )
            ListItem(
                modifier = Modifier.clickable {  },
                text = { Text(text = stringResource(id = R.string.feedback)) },
                trailing = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = ""
                    )
                }
            )
            ListItem(
                modifier = Modifier.clickable {  },
                text = { Text(text = stringResource(id = R.string.update)) },
                trailing = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = ""
                    )
                }
            )
            ListItem(
                modifier = Modifier.clickable {  },
                text = { Text(text = stringResource(id = R.string.privacy_policy)) },
                trailing = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = ""
                    )
                }
            )
            ListItem(
                modifier = Modifier.clickable {  },
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