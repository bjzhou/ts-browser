package com.hinnka.tsbrowser.ui.composable.widget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable

@Composable
fun TSBackHandler(enabled: Boolean = true, onBack: () -> Unit, content: @Composable () -> Unit) {
    Box {
        BackHandler(enabled, onBack)
        content()
    }
}