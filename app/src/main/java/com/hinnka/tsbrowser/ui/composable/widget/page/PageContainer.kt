package com.hinnka.tsbrowser.ui.composable.widget.page

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ui.composable.widget.TSBackHandler

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PageContainer(start: String, pageGraph: PageGraphBuilder.() -> Unit) {
    logD("PageContainer start")
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
    logD("PageContainer end")
}