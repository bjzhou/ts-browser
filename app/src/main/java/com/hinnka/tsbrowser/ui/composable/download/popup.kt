package com.hinnka.tsbrowser.ui.composable.download

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.hinnka.tsbrowser.R

@Composable
fun LongPressPopup(showPopup: MutableState<Boolean>, onCopy: () -> Unit, onDelete: () -> Unit) {
    if (showPopup.value) {
        Popup(
            alignment = Alignment.Center,
            offset = IntOffset(0, 100),
            onDismissRequest = {
                showPopup.value = false
            },
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = 4.dp,
                modifier = Modifier.wrapContentWidth().height(99.dp)
            ) {
                val heightAnim = remember { Animatable(0.dp, Dp.VectorConverter) }
                LaunchedEffect(key1 = heightAnim) {
                    heightAnim.animateTo(48.dp)
                }
                Column(
                    modifier = Modifier.width(100.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(heightAnim.value)
                            .fillMaxWidth()
                            .clickable {
                                onCopy()
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = stringResource(id = R.string.copy_link),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(heightAnim.value)
                            .fillMaxWidth()
                            .clickable {
                                onDelete()
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = stringResource(id = R.string.delete),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}