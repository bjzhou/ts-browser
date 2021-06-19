package com.hinnka.tsbrowser.ui.composable.widget.page

import androidx.compose.runtime.Composable

object PageGraphBuilder {
    val pages = mutableMapOf<String, @Composable (Array<out Any>?) -> Unit>()

    fun page(route: String, content: @Composable (Array<out Any>?) -> Unit) {
        pages[route] = content
    }
}
