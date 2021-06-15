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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.ui.LocalViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun TSBottomDrawer(
    modifier: Modifier = Modifier,
    drawerState: BottomDrawerState = remember { BottomDrawerState() },
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    content: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val viewModel = LocalViewModel.current
    val density = LocalDensity.current
    val statusBarHeight = statusBarHeight()


    val keyboardIsHiding = remember { mutableStateOf(false) }

    var waitForShow by drawerState.waitForShow
    val imeHeight = viewModel.imeHeightState.value
    if (imeHeight > 0 && !drawerState.isClosed && !drawerState.isClosing && !keyboardIsHiding.value) {
        waitForShow = true
    }

    if (imeHeight == 0f) {
        keyboardIsHiding.value = false
    }

    if (drawerState.isClosing) {
        keyboardIsHiding.value = true
        LocalFocusManager.current.clearFocus()
    }

    if (drawerState.isClosed) {
        drawerState.isClosing = false
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val fullHeight = constraints.maxHeight.toFloat()
        val maxHeight = fullHeight - with(density) { statusBarHeight.toPx() }

        LaunchedEffect(key1 = fullHeight) {
            drawerState.init(fullHeight)
        }

        val blockClicks = if (drawerState.isClosed) {
            Modifier
        } else {
            Modifier.pointerInput(Unit) { detectTapGestures {} }
        }
        val drawerConstraints = with(density) {
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
            if (drawerState.showScrimState.value) {
                BottomDrawerScrim(
                    color = DrawerDefaults.scrimColor,
                    onDismiss = {
                        if (drawerState.cancelable) {
                            scope.launch { drawerState.close() }
                        }
                    },
                    visible = !drawerState.isClosed || waitForShow
                )
            }
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
                    .graphicsLayer {
                        translationY = -viewModel.imeHeightState.value
                    }
                    .offset { IntOffset(x = 0, y = drawerState.offset.value.roundToInt()) },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = drawerBackgroundColor,
                contentColor = drawerContentColor,
                elevation = DrawerDefaults.Elevation
            ) {
                Column(
                    blockClicks.heightIn(max = with(density) { maxHeight.toDp() }),
                    content = drawerState.drawerContent
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
class BottomDrawerState {

    private var fullHeight = 0f
    internal val offset = Animatable(fullHeight)

    internal var drawerContent: @Composable ColumnScope.() -> Unit = {}

    val waitForShow = mutableStateOf(false)
    var showScrimState = mutableStateOf(true)

    var cancelable = true

    var isClosing = false

    val isClosed: Boolean
        get() = offset.value == fullHeight

    suspend fun init(fullHeight: Float) {
        this.fullHeight = fullHeight
        offset.snapTo(fullHeight)
    }

    fun open(showScrim: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
        showScrimState.value = showScrim
        drawerContent = content
        waitForShow.value = true
    }

    suspend fun close() {
        isClosing = true
        offset.animateTo(fullHeight)
        drawerContent = {}
    }
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