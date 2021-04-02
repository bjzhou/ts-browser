package com.hinnka.tsbrowser.ext

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.removeFromParent() {
    val parent = parent as? ViewGroup
    parent?.removeView(this)
}

fun View.setFullScreen(enable: Boolean) {
    val controller = ViewCompat.getWindowInsetsController(this)
    if (enable) {
        controller?.hide(WindowInsetsCompat.Type.systemBars())
    } else {
        controller?.show(WindowInsetsCompat.Type.systemBars())
    }
}

val View.activity: Activity?
    get() = context as? Activity