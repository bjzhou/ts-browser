package com.hinnka.tsbrowser.web

import android.graphics.Bitmap
import android.os.Message
import androidx.compose.runtime.MutableState

interface WebDataListener {
    val progressState: MutableState<Float>
    val urlState: MutableState<String>
    val titleState: MutableState<String>
    val iconState: MutableState<Bitmap?>
    val previewState: MutableState<Bitmap?>
    val canGoBackState: MutableState<Boolean>
    val canGoForwardState: MutableState<Boolean>

    fun onCreateWindow(message: Message)
    fun onCloseWindow()
    suspend fun updateInfo()
}