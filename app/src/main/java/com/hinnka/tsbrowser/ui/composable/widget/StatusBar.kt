package com.hinnka.tsbrowser.ui.composable.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Composable
fun StatusBar() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(statusBarHeight())
        .background(MaterialTheme.colors.surface)
    )
}

@Composable
fun statusBarHeight(): Dp {
    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view)
    val height = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: return 0.dp
    val density = LocalDensity.current
    return with(density) { height.toDp() }
}