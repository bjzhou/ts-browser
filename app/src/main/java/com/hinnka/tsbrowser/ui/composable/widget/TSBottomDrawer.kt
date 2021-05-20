package com.hinnka.tsbrowser.ui.composable.widget

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
import com.hinnka.tsbrowser.ext.logD
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@ExperimentalMaterialApi
fun TSBottomDrawer(
    modifier: Modifier = Modifier,
    drawerState: BottomDrawerState = remember { BottomDrawerState() },
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    var waitForShow by drawerState.waitForShow
//    logD("TSBottomDrawer $waitForShow")

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
                visible = !drawerState.isClosed || waitForShow
            )
            Surface(
                drawerConstraints
                    .onGloballyPositioned { position ->
                        if (waitForShow) {
                            val drawerHeight = position.size.height.toFloat()
                            scope.launch {
                                drawerState.offset.animateTo(fullHeight - drawerHeight)
                            }
                            waitForShow = false
                        }
                    }
                    .offset { IntOffset(x = 0, y = drawerState.offset.value.roundToInt()) },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = drawerBackgroundColor,
                contentColor = drawerContentColor,
                elevation = DrawerDefaults.Elevation
            ) {
                Column(blockClicks, content = drawerState.drawerContent)
            }
        }
    }
}

class BottomDrawerState {

    private var fullHeight = 0f
    internal val offset = Animatable(fullHeight)

    internal var drawerContent: @Composable ColumnScope.() -> Unit = {}

    val waitForShow = mutableStateOf(false)

    val isClosed: Boolean
        get() = offset.value == fullHeight

    suspend fun init(fullHeight: Float) {
        this.fullHeight = fullHeight
        offset.snapTo(fullHeight)
    }

    suspend fun open(content: @Composable ColumnScope.() -> Unit) {
        drawerContent = content
        waitForShow.value = true
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