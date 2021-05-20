package com.hinnka.tsbrowser.persist

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import com.google.gson.Gson
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ext.asMutable
import com.hinnka.tsbrowser.ext.md5
import com.hinnka.tsbrowser.tab.TabManager

object Settings {
    private val pref = App.instance.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val gson = Gson()

    object Default {
        val searchEngine = NameValue("Google", "https://www.google.com/search?q=%s")
        val userAgent = NameValue("Android", WebSettings.getDefaultUserAgent(App.instance))
        val adblock = true
        val acceptThirdPartyCookies = true
        val dnt = false
    }

    var searchEngine: NameValue
        get() {
            val persist = pref.getString("searchEngine", null) ?: return Default.searchEngine
            return gson.fromJson(persist, NameValue::class.java)
        }
        set(value) {
            pref.edit { putString("searchEngine", gson.toJson(value)) }
            searchEngineState.asMutable().value = value
        }

    var userAgent: NameValue
        get() {
            val persist = pref.getString("userAgent", null) ?: return Default.userAgent
            return gson.fromJson(persist, NameValue::class.java)
        }
        set(value) {
            pref.edit { putString("userAgent", gson.toJson(value)) }
            userAgentState.asMutable().value = value
            TabManager.currentTab.value?.view?.let {
                it.settings.userAgentString = value.value
                it.reload()
                it.post {
                    it.settings.loadWithOverviewMode = false
                    it.settings.loadWithOverviewMode = true
                }
            }
        }

    var mnemonic: String?
        get() {
            return pref.getString("mnemonic", null)
        }
        set(value) {
            pref.edit { putString("mnemonic", value.md5()) }
        }

    var adblock: Boolean
        get() = pref.getBoolean("adblock", Default.adblock)
        set(value) {
            pref.edit { putBoolean("adblock", value) }
            adblockState.asMutable().value = value
            TabManager.currentTab.value?.view?.reload()
        }

    var acceptThirdPartyCookies: Boolean
        get() = pref.getBoolean("acceptThirdpartyCookies", Default.acceptThirdPartyCookies)
        set(value) {
            pref.edit { putBoolean("acceptThirdpartyCookies", value) }
            acceptThirdPartyCookiesState.asMutable().value = value
            TabManager.currentTab.value?.view?.let {
                CookieManager.getInstance().setAcceptThirdPartyCookies(it, value)
            }
        }

    var dnt: Boolean
        get() = pref.getBoolean("dnt", Default.dnt)
        set(value) {
            pref.edit { putBoolean("dnt", value) }
            dntState.asMutable().value = value
        }


    val searchEngineState: State<NameValue> = mutableStateOf(searchEngine)
    val userAgentState: State<NameValue> = mutableStateOf(userAgent)
    val mnemonicState: State<String?> = mutableStateOf(mnemonic)
    val adblockState: State<Boolean> = mutableStateOf(adblock)
    val acceptThirdPartyCookiesState: State<Boolean> = mutableStateOf(acceptThirdPartyCookies)
    val dntState: State<Boolean> = mutableStateOf(dnt)
}


object SettingOptions {
    val searchEngine = listOf(
        Settings.Default.searchEngine,
        NameValue("Bing", "https://wwww.bing.com/search?q=%s"),
        NameValue("DuckDuckGo", "https://duckduckgo.com/?q=%s")
    )
    val userAgent = listOf(
        Settings.Default.userAgent,
        NameValue(
            "iPhone",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.2 Mobile/15E148 Safari/604.1"
        ),
        NameValue(
            "PC",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36"
        ),
        NameValue(
            "iPad",
            "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1 Mobile/15E148 Safari/604.1"
        ),
        NameValue(
            "Mac",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15"
        ),
    )
}

data class NameValue(val name: String, val value: String)