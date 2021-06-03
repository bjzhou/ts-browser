package com.hinnka.tsbrowser.persist

import android.content.Context
import androidx.core.content.edit
import com.hinnka.tsbrowser.App

object LocalStorage {
    private val pref = App.instance.getSharedPreferences("localStorage.${App.processName}", Context.MODE_PRIVATE)

    var isFavoriteInitialized
        get() = pref.getBoolean("isFavoriteInitialized", false)
        set(value) = pref.edit { putBoolean("isFavoriteInitialized", value) }
}