package com.hinnka.tsbrowser

import android.app.Application
import com.tencent.mmkv.MMKV

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        MMKV.initialize(this)
    }

    companion object {

        @JvmStatic
        lateinit var instance: App

    }
}