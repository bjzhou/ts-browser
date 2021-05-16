package com.hinnka.tsbrowser.ui.composable.main

import android.webkit.WebView
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.DpOffset
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.home.LongPressInfo
import com.hinnka.tsbrowser.ui.home.hidePopup
import com.hinnka.tsbrowser.ui.LocalViewModel
import zlc.season.rxdownload4.download
import zlc.season.rxdownload4.file

@Composable
fun LongPressPopup() {
    val tabOpt by TabManager.currentTab
    val tab = tabOpt ?: return
    val density = LocalDensity.current
    val info by tab.longPressState
    DropdownMenu(
        expanded = info.show,
        offset = with(density) { DpOffset(info.xOffset.toDp(), info.yOffset.toDp()) },
        onDismissRequest = {
            info.hidePopup()
        },
    ) {
        when (info.type) {
            WebView.HitTestResult.EMAIL_TYPE, WebView.HitTestResult.PHONE_TYPE -> {
                Copy(info)
            }
            WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                OpenInNewTab(info)
                OpenInBackgroundTab(info)
                CopyLink(info)
                ShareLink(info)
            }
            WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                OpenInNewTab(info)
                OpenInBackgroundTab(info)
                SaveImage(info)
                CopyLink(info)
                ShareImage(info)
            }
            else -> {
            }
        }
    }
}

@Composable
fun OpenInNewTab(info: LongPressInfo) {
    val context = LocalContext.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        TabManager.newTab(context).apply {
            loadUrl(info.extra)
            active()
        }
    }) {
        Text(text = stringResource(id = R.string.open_in_new_tab))
    }
}

@Composable
fun OpenInBackgroundTab(info: LongPressInfo) {
    val context = LocalContext.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        TabManager.newTab(context).apply {
            loadUrl(info.extra)
        }
    }) {
        Text(text = stringResource(id = R.string.open_in_background_tab))
    }
}

@Composable
fun CopyLink(info: LongPressInfo) {
    val clipboardManager = LocalClipboardManager.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        clipboardManager.setText(AnnotatedString(info.extra))
    }) {
        Text(text = stringResource(id = R.string.copy_link))
    }
}

@Composable
fun Copy(info: LongPressInfo) {
    val clipboardManager = LocalClipboardManager.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        clipboardManager.setText(AnnotatedString(info.extra))
    }) {
        Text(text = stringResource(id = R.string.copy))
    }
}

@Composable
fun ShareLink(info: LongPressInfo) {
    val viewModel = LocalViewModel.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        viewModel.share(info.extra)
    }) {
        Text(text = stringResource(id = R.string.share_link))
    }
}

@Composable
fun SaveImage(info: LongPressInfo) {
    val context = LocalContext.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        DownloadHandler(context).downloadImage(info.extra)
    }) {
        Text(text = stringResource(id = R.string.save_image))
    }
}

@Composable
fun ShareImage(info: LongPressInfo) {
    val viewModel = LocalViewModel.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        info.extra.download().doOnComplete {
            viewModel.share(info.extra.file())
        }.subscribe()
    }) {
        Text(text = stringResource(id = R.string.share_image))
    }
}