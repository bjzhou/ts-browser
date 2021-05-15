package com.hinnka.tsbrowser.ext

import android.graphics.Point
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalView

@Composable
fun screenSize(): Point {
    val realSize = Point()
    LocalView.current.display.getRealSize(realSize)
    return realSize
}

fun <T> State<T>.asMutable(): MutableState<T> {
    return this as MutableState<T>
}