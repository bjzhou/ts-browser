package com.hinnka.tsbrowser

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.util.Log
import android.webkit.WebView
import com.hinnka.tsbrowser.ext.logE
import io.reactivex.plugins.RxJavaPlugins
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        configWebViewCacheDirWithAndroidP()
        RxJavaPlugins.setErrorHandler {
            logE("RxJava run error", throwable = it)
        }
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

        val isSecretMode: Boolean by lazy { getProcessName().endsWith("secret") }
    }
}