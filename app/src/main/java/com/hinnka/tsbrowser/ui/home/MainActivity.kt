package com.hinnka.tsbrowser.ui.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.base.PageContainer
import com.hinnka.tsbrowser.ui.base.PageController
import com.hinnka.tsbrowser.ui.composable.download.DownloadPage
import com.hinnka.tsbrowser.ui.composable.main.MainPage
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme
import com.hinnka.tsbrowser.viewmodel.HomeViewModel
import com.hinnka.tsbrowser.viewmodel.LocalViewModel

open class MainActivity : BaseActivity() {

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Providers {
                TSBrowserTheme {
                    PageContainer("main") {
                        page("main") { MainPage() }
                        page("downloads") { DownloadPage() }
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            TabManager.loadTabs(this@MainActivity)
        }
    }

    @Composable
    fun Providers(content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalViewModel provides viewModel,
            content = content)
    }

    override fun onResume() {
        super.onResume()
        TabManager.onResume(viewModel.uiState.value)
    }

    override fun onPause() {
        super.onPause()
        TabManager.onPause()
    }

    override fun onBackPressed() {
        if (PageController.routes.size > 1) {
            PageController.navigateUp()
            return
        }
        if (viewModel.uiState.value != UIState.Main) {
            viewModel.uiState.value = UIState.Main
            return
        }
        if (TabManager.currentTab.value?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}