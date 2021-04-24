package com.hinnka.tsbrowser.ext

import android.graphics.Point
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
fun screenSize(): Point {
    val realSize = Point()
    LocalView.current.display.getRealSize(realSize)
    return realSize
}