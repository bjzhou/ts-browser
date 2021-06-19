package com.hinnka.tsbrowser.ui.composable.main.drawer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.LocalStorage

@Composable
fun PrivacyContent() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x66000000), RoundedCornerShape(8.dp))
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "days",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp),
                    tint = MaterialTheme.colors.primary
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.privacy_protect),
                        fontSize = 13.sp,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = LocalStorage.protectDays.toString(),
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.alignByBaseline(),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(id = R.string.days),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .alignByBaseline(),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "adblock",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp),
                    tint = Color.Red
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.ads_blocked),
                        fontSize = 13.sp,
                    )
                    Row {
                        Text(
                            text = LocalStorage.blockTimesState.value.toString(),
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.alignByBaseline(),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(id = R.string.times),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .alignByBaseline(),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}