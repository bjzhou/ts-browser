package com.hinnka.tsbrowser.ui.composable.widget

import androidx.compose.animation.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntOffset
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

object PageGraphBuilder {
    val pages = mutableMapOf<String, @Composable (Array<out Any>?) -> Unit>()

    fun page(route: String, content: @Composable (Array<out Any>?) -> Unit) {
        pages[route] = content
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PageContainer(start: String, pageGraph: PageGraphBuilder.() -> Unit) {
    if (PageController.routes.isEmpty()) {
        PageController.set(start)
    }
    PageGraphBuilder.pageGraph()
    PageController.routes.forEach { route ->
        Page(route = route) {
            TSBackHandler(
                enabled = PageController.routes.size > 1,
                onBack = { PageController.navigateUp() }) {
                PageGraphBuilder.pages[route]?.invoke(PageController.argumentMap[route])
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Page(
    route: String,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = PageController.currentRoute.value == route,
        enter = if (PageController.addAnimPending) {
            slideIn(initialOffset = { size ->
                IntOffset(
                    size.width,
                    0
                )
            })
        } else {
            slideIn(initialOffset = { size ->
                IntOffset(
                    -size.width / 3,
                    0
                )
            })
        },
        exit = if (PageController.removeAnimPending) slideOut(targetOffset = { size ->
            IntOffset(
                size.width,
                0
            )
        }) else slideOut(targetOffset = { size ->
            IntOffset(
                -size.width / 3,
                0
            )
        })
    ) {
        Surface {
            content()
        }
    }

}