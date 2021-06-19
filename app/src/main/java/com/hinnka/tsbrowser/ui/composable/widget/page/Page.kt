package com.hinnka.tsbrowser.ui.composable.widget.page

import androidx.compose.animation.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntOffset
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.mainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Page(
    route: String,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = PageController.currentRoute.value == route,
        enter = if (PageController.addAnimPending) {
            slideIn(initialOffset = { size ->
                IntOffset(
                    size.width,
                    0
                )
            })
        } else {
            slideIn(initialOffset = { size ->
                IntOffset(
                    -size.width / 3,
                    0
                )
            })
        },
        exit = if (PageController.removeAnimPending) slideOut(targetOffset = { size ->
            IntOffset(
                size.width,
                0
            )
        }) else slideOut(targetOffset = { size ->
            IntOffset(
                -size.width / 3,
                0
            )
        })
    ) {
        Surface {
            content()
        }
    }

}