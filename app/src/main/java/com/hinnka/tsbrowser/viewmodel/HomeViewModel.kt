package com.hinnka.tsbrowser.viewmodel

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.db.AppDatabase
import com.hinnka.tsbrowser.db.SearchHistory
import com.hinnka.tsbrowser.ext.*
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.home.SecretActivity
import com.hinnka.tsbrowser.ui.home.UIState
import com.hinnka.tsbrowser.util.IconCache
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel : ViewModel() {
    val uiState = mutableStateOf(UIState.Main)
    val searchList = mutableStateListOf<SearchHistory>()
    val addressText = mutableStateOf(TextFieldValue())
    val imeHeightState = Animatable(0f)

    fun onGo(text: String, context: Context) {
        val urlText = text.trim()
        if (urlText.isBlank()) {
            return
        }
        if (urlText == "900902") {
            if (App.isSecretMode) {
                (context as? Activity)?.finish()
            } else {
                context.startActivity(Intent(context, SecretActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                })
            }
            return
        }
        TabManager.currentTab.value?.loadUrl(urlText.toUrl())
        ioScope.launch {
            val searchHistory = SearchHistory(
                if (urlText.isUrl()) urlText.toUrl() else urlText,
                System.currentTimeMillis(),
            )
            val searchDao = AppDatabase.instance.searchHistoryDao()
            searchDao.insert(searchHistory)
            val allList = searchDao.getAll()
            if (allList.size > 10) {
                searchDao.delete(*allList.subList(0, allList.size - 10).toTypedArray())
            }
        }
    }

    fun loadSearchHistory() {
        ioScope.launch {
            val list = AppDatabase.instance.searchHistoryDao().getAll().asReversed()
            list.forEach {
                it.iconBitmap = IconCache[it.url?.host ?: ""]
            }
            searchList.clear()
            searchList.addAll(list)
        }
    }

    fun delete(history: SearchHistory) {
        ioScope.launch {
            AppDatabase.instance.searchHistoryDao().delete(history)
            searchList.remove(history)
        }
    }

    fun copy(text: String) {
        App.instance.clipboardManager?.setPrimaryClip(ClipData.newPlainText("plain text", text))
    }

    fun editInAddressBar(url: String) {
        addressText.value = TextFieldValue(url, TextRange(url.length, url.length))
    }

    fun share(url: String, title: String = "") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, url)
            if (title.isNotBlank()) {
                putExtra(Intent.EXTRA_TITLE, title)
            }
            type = "plain/text"
        }
        val chooser = Intent.createChooser(intent, App.instance[R.string.send_to]).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        App.instance.startActivity(chooser)
    }

    fun share(image: File) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            val uri = FileProvider.getUriForFile(
                App.instance,
                App.instance.packageName + ".provider",
                image
            )
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val chooser = Intent.createChooser(intent, App.instance[R.string.send_to]).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        App.instance.startActivity(chooser)
    }
}

val LocalViewModel = staticCompositionLocalOf<HomeViewModel> {
    error("no view model provided")
}