package com.hinnka.tsbrowser

import android.app.Application
import android.os.Build
import android.webkit.WebView
import com.tencent.mmkv.MMKV
import android.annotation.SuppressLint
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        MMKV.initialize(this)
        configWebViewCacheDirWithAndroidP()
    }

    private fun configWebViewCacheDirWithAndroidP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            if (packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }
    }

    companion object {

        @JvmStatic
        lateinit var instance: App


        @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
        fun getProcessName(): String {
            return if (Build.VERSION.SDK_INT >= 28) Application.getProcessName() else try {
                val activityThread = Class.forName("android.app.ActivityThread")
                val methodName = "currentProcessName"
                val getProcessName: Method = activityThread.getDeclaredMethod(methodName)
                getProcessName.invoke(null) as String
            } catch (e: ClassNotFoundException) {
                throw RuntimeException(e)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e)
            }
        }
    }
}