package com.hinnka.tsbrowser.web

import android.graphics.Bitmap
import android.os.Message
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.hinnka.tsbrowser.ui.home.LongPressInfo

interface WebDataListener {
    val progressState: MutableState<Float>
    val urlState: MutableState<String>
    val titleState: MutableState<String>
    val iconState: MutableState<Bitmap?>
    val previewState: MutableState<Bitmap?>
    val canGoBackState: MutableState<Boolean>
    val canGoForwardState: MutableState<Boolean>
    val longPressState: MutableState<LongPressInfo>

    fun onCreateWindow(message: Message)
    fun onCloseWindow()
    suspend fun updateInfo()
}