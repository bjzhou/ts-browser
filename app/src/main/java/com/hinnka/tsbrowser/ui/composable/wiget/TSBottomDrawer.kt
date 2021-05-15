package com.hinnka.tsbrowser.ui.composable.wiget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@ExperimentalMaterialApi
fun TSBottomDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: BottomDrawerState = remember { BottomDrawerState() },
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier.fillMaxSize()) {
        val fullHeight = constraints.maxHeight.toFloat()

        LaunchedEffect(key1 = fullHeight) {
            drawerState.init(fullHeight)
        }

        val blockClicks = if (drawerState.isClosed) {
            Modifier
        } else {
            Modifier.pointerInput(Unit) { detectTapGestures {} }
        }
        val drawerConstraints = with(LocalDensity.current) {
            Modifier
                .sizeIn(
                    maxWidth = constraints.maxWidth.toDp(),
                    maxHeight = constraints.maxHeight.toDp()
                )
        }

        Box {
            TSBackHandler(enabled = !drawerState.isClosed, onBack = {
                scope.launch {
                    drawerState.close()
                }
            }) {
                content()
            }
            BottomDrawerScrim(
                color = DrawerDefaults.scrimColor,
                onDismiss = { scope.launch { drawerState.close() } },
                visible = !drawerState.isClosed
            )
            Surface(
                drawerConstraints
                    .onGloballyPositioned { position ->
                        if (drawerState.drawerHeight <= 0f) {
                            drawerState.drawerHeight = position.size.height.toFloat()
                        }
                    }
                    .offset { IntOffset(x = 0, y = drawerState.offset.value.roundToInt()) },
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                color = drawerBackgroundColor,
                contentColor = drawerContentColor,
                elevation = DrawerDefaults.Elevation
            ) {
                Column(blockClicks, content = drawerContent)
            }
        }
    }
}

class BottomDrawerState {

    private var fullHeight = 0f
    internal val offset = Animatable(fullHeight)

    var drawerHeight = 0f

    val isClosed: Boolean
        get() = offset.value == fullHeight

    suspend fun init(fullHeight: Float) {
        this.fullHeight = fullHeight
        offset.snapTo(fullHeight)
    }

    suspend fun open() {
        offset.animateTo(fullHeight - drawerHeight)
    }

    suspend fun close() = offset.animateTo(fullHeight)
}

@Composable
private fun BottomDrawerScrim(
    color: Color,
    onDismiss: () -> Unit,
    visible: Boolean
) {
    if (color != Color.Transparent) {
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = TweenSpec()
        )
        val dismissModifier = if (visible) {
            Modifier
                .pointerInput(onDismiss) {
                    detectTapGestures { onDismiss() }
                }
        } else {
            Modifier
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .then(dismissModifier)
        ) {
            drawRect(color = color, alpha = alpha)
        }
    }
}