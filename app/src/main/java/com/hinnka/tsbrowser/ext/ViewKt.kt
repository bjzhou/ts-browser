package com.hinnka.tsbrowser.ext

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.removeFromParent() {
    val parent = parent as? ViewGroup
    parent?.removeView(this)
}

fun View.setFullScreen(enable: Boolean) {
    val controller = ViewCompat.getWindowInsetsController(this)
    if (enable) {
        controller?.hide(WindowInsetsCompat.Type.statusBars())
    } else {
        controller?.show(WindowInsetsCompat.Type.statusBars())
    }
}

val View.activity: Activity?
    get() = context as? Activity

fun Modifier.tap(block: (Offset) -> Unit): Modifier {
    return pointerInput(Unit) {
        detectTapGestures(onTap = block)
    }
}