package com.hinnka.tsbrowser.web

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.webkit.JavascriptInterface
import com.hinnka.tsbrowser.ext.activity
import com.hinnka.tsbrowser.persist.Settings

class TSBridge(val webView: TSWebView) {

    private val ANY = "any"
    private val PORTRAIT_PRIMARY = "portrait-primary"
    private val PORTRAIT_SECONDARY = "portrait-secondary"
    private val LANDSCAPE_PRIMARY = "landscape-primary"
    private val LANDSCAPE_SECONDARY = "landscape-secondary"
    private val PORTRAIT = "portrait"
    private val LANDSCAPE = "landscape"

    @SuppressLint("SourceLockedOrientationActivity")
    @JavascriptInterface
    fun requestScreenOrientation(orientation: String) {
        val activity = webView.activity ?: return
        when (orientation) {
            ANY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            LANDSCAPE_PRIMARY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            PORTRAIT_PRIMARY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            LANDSCAPE -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            PORTRAIT -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
            LANDSCAPE_SECONDARY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
            PORTRAIT_SECONDARY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            }
        }
    }

    @JavascriptInterface
    fun isDarkMode(): Boolean {
        return Settings.darkMode
    }
}