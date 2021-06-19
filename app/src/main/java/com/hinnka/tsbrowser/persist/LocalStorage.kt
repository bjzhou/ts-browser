package com.hinnka.tsbrowser.persist

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ext.asMutable
import java.util.concurrent.TimeUnit

object LocalStorage {
    private val pref = App.instance.getSharedPreferences("localStorage.${App.processName}", Context.MODE_PRIVATE)
    private val prefGlobal = App.instance.getSharedPreferences("localStorage", Context.MODE_PRIVATE)

    var isFavoriteInitialized
        get() = pref.getBoolean("isFavoriteInitialized", false)
        set(value) = pref.edit { putBoolean("isFavoriteInitialized", value) }

    val protectDays: Long
        get() {
            val appInfo = App.instance.packageManager.getPackageInfo(App.instance.packageName, 0)
            return (System.currentTimeMillis() - appInfo.firstInstallTime) / TimeUnit.DAYS.toMillis(1)
        }

    var blockTimes: Long
        get() = prefGlobal.getLong("blockTimes", 0L)
        set(value) {
            prefGlobal.edit { putLong("blockTimes", value) }
            blockTimesState.asMutable().value = value
        }

    val blockTimesState: State<Long> = mutableStateOf(blockTimes)

    var isSecretVisited
        get() = prefGlobal.getBoolean("isSecretVisited", false)
        set(value) = prefGlobal.edit { putBoolean("isSecretVisited", value) }

    var lastClickDefaultBrowserTime
        get() = prefGlobal.getLong("lastClickDefaultBrowserTime", 0)
        set(value) = prefGlobal.edit { putLong("lastClickDefaultBrowserTime", value) }
}