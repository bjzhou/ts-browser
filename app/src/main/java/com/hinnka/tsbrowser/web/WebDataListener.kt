package com.hinnka.tsbrowser.web

import android.graphics.Bitmap
import android.os.Message
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.hinnka.tsbrowser.ui.home.LongPressInfo

interface WebDataListener {
    val progressState: MutableState<Float>
    val titleState: MutableState<String>
    val previewState: MutableState<Bitmap?>
    val longPressState: MutableState<LongPressInfo>

    fun onCreateWindow(message: Message)
    fun onCloseWindow()
    suspend fun updateInfo()
    fun doUpdateVisitedHistory(url: String, isReload: Boolean)
    fun onReceivedIcon(icon: Bitmap?)
}