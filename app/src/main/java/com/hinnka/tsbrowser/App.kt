package com.hinnka.tsbrowser

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {

        @JvmStatic
        lateinit var instance: App

    }
}