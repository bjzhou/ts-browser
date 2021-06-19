package com.hinnka.tsbrowser.ui.composable.widget.page

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.mainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PageController {
    val routes = mutableStateListOf<String>()
    val argumentMap = mutableStateMapOf<String, Array<out Any>>()
    val currentRoute = mutableStateOf<String?>(null)
    var addAnimPending = false
    var removeAnimPending = false

    fun set(route: String, vararg arguments: Any) {
        argumentMap[route] = arguments
        routes.add(route)
        currentRoute.value = route
    }

    fun navigate(route: String, vararg arguments: Any) {
        addAnimPending = true
        argumentMap[route] = arguments
        routes.add(route)
        ioScope.launch {
            delay(50)
            currentRoute.value = route
            delay(250)
            addAnimPending = false
        }
    }

    fun navigateUp() {
        removeAnimPending = true
        currentRoute.value = if (routes.size > 1) routes.asReversed()[1] else null
        mainScope.launch {
            delay(250)
            val last = routes.removeLastOrNull() ?: return@launch
            argumentMap.remove(last)
            removeAnimPending = false
        }
    }
}