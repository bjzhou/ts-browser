package com.hinnka.tsbrowser

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.logE
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.Favorites
import io.reactivex.plugins.RxJavaPlugins
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        logD("$processName onCreate")
        instance = this
        configWebViewCacheDirWithAndroidP()
        RxJavaPlugins.setErrorHandler {
            logE("RxJava run error", throwable = it)
        }
        sendBroadcast(Intent("${packageName}.action.secret").apply {
            `package` = packageName
        })
        initBrowser()
        logD("$processName onCreate complete")
    }

    private fun initBrowser() {
        if (processName != packageName) {
            //FIXME hack for RxDownload init in sub process
            contentResolver.insert(Uri.parse("content://$packageName.claritypotion/start"), null)
        }
        Bookmark.init()
        Favorites.init()
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

        val processName: String by lazy {
            if (Build.VERSION.SDK_INT >= 28) getProcessName() else try {
                @SuppressLint("PrivateApi")
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

        val isSecretMode: Boolean by lazy { processName.endsWith("secret") }

        @SuppressLint("ConstantLocale")
        val isCN: Boolean = Locale.getDefault().country.toUpperCase(Locale.ROOT) == "CN"
    }
}