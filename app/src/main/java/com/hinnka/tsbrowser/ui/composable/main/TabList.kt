package com.hinnka.tsbrowser.ui.composable.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.tsbrowser.ext.host
import com.hinnka.tsbrowser.ext.tap
import com.hinnka.tsbrowser.persist.IconMap
import com.hinnka.tsbrowser.tab.Tab
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.composable.widget.statusBarHeight
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.ui.LocalViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
//TODO refactor animation
fun TabList() {
    val viewModel = LocalViewModel.current
    val tabs = TabManager.tabs
    val screenWidth = LocalView.current.width
    val screenHeight = LocalView.current.height
    val targetOffset = remember { mutableStateOf(IntOffset.Zero) }
    val targetSize = remember { mutableStateOf(IntSize(screenWidth, screenHeight)) }
    val density = LocalDensity.current
    val showAnim = remember { mutableStateOf(true) }
    val hideAnim = remember { mutableStateOf(false) }
    val resetAnim = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val statusBarHeight = statusBarHeight()

    LazyVerticalGrid(
        cells = GridCells.Fixed(2),
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(8.dp)
    ) {
        items(tabs.size) {
            val tab = tabs[it]
            Card(
                elevation = 2.dp, modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .padding(8.dp)
                    .onGloballyPositioned { coor ->
                        if (tab.info.isActive && (showAnim.value || resetAnim.value)) {
                            val width = coor.size.width - density.run { 4.dp.toPx() }
                            val height = coor.size.height - density.run { 32.dp.toPx() }
                            targetSize.value = IntSize(width.toInt(), height.toInt())
                            val offset = coor.positionInRoot()
                            val x = (offset.x + density.run { 2.dp.toPx() }).toInt()
                            val y =
                                (offset.y + density.run { (30.dp - statusBarHeight).toPx() }).toInt()
                            targetOffset.value = IntOffset(x, y)
                        }
                    }
            ) {
                TabItem(tab = tabs[it]) {
                    val lastActive = tab.info.isActive
                    tab.active()
                    if (tab.previewState.value != null) {
                        if (lastActive) {
                            hideAnim.value = true
                            targetOffset.value = IntOffset.Zero
                            targetSize.value = IntSize(screenWidth, screenHeight)
                        } else {
                            resetAnim.value = true
                        }
                    } else {
                        viewModel.uiState.value = UIState.Main
                    }
                }
            }
        }
    }

    val tab = tabs.firstOrNull { it.info.isActive } ?: return
    val preview = tab.previewState.value ?: return
    LaunchedEffect(listState) {
        listState.scrollToItem(tabs.indexOf(tab) / 2)
    }
    val offsetAnimate = animateIntOffsetAsState(
        targetValue = targetOffset.value,
        animationSpec = if (resetAnim.value) tween(0) else spring()
    ) { offset ->
        if (showAnim.value && offset != IntOffset.Zero) {
            showAnim.value = false
        } else if (resetAnim.value) {
            resetAnim.value = false
            hideAnim.value = true
            targetOffset.value = IntOffset.Zero
            targetSize.value = IntSize(screenWidth, screenHeight)
        } else if (hideAnim.value) {
            hideAnim.value = false
            viewModel.uiState.value = UIState.Main
        }
    }
    val widthAnim = animateDpAsState(
        targetValue = density.run { targetSize.value.width.toDp() },
        animationSpec = if (resetAnim.value) tween(0) else spring()
    )
    val heightAnim = animateDpAsState(
        targetValue = density.run { targetSize.value.height.toDp() },
        animationSpec = if (resetAnim.value) tween(0) else spring()
    )

    if (showAnim.value || hideAnim.value) {
        Image(
            bitmap = preview.asImageBitmap(),
            modifier = Modifier
                .absoluteOffset { offsetAnimate.value }
                .size(widthAnim.value, heightAnim.value),
            contentDescription = tab.urlState.value,
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
fun TabItem(tab: Tab, onTap: () -> Unit) {
    val context = LocalContext.current
    val viewModel = LocalViewModel.current
    val title = tab.titleState
    val preview = tab.previewState
    val url = tab.urlState
    val icon = IconMap[url.value]
    Column(modifier = Modifier
        .tap {
            onTap()
        }
        .border(
            width = if (tab.info.isActive) 2.dp else 0.dp,
            color = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(4.dp)
        )) {
        Row(modifier = Modifier.height(28.dp), verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(22.dp)
                        .padding(start = 3.dp)
                )
            }
            Text(
                text = title.value,
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 3.dp)
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        val index = TabManager.tabs.indexOf(tab)
                        TabManager.remove(tab)
                        if (tab.info.isActive) {
                            when {
                                TabManager.tabs.size > index -> {
                                    TabManager.tabs[index].active()
                                }
                                TabManager.tabs.isNotEmpty() -> {
                                    TabManager.tabs
                                        .last()
                                        .active()
                                }
                                else -> {
                                    TabManager.newTab(context).apply {
                                        goHome()
                                        active()
                                    }
                                    viewModel.uiState.value = UIState.Main
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(22.dp)
                        .padding(3.dp)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(0.5f.dp)
                .fillMaxWidth()
                .background(Color.LightGray)
        )
        preview.value?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentDescription = tab.urlState.value,
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.FillWidth
            )
        }
    }
}