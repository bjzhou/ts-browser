package com.hinnka.tsbrowser.web

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.ext.*
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.home.LongPressInfo
import com.hinnka.tsbrowser.ui.home.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.coroutines.resume

@SuppressLint("SetJavaScriptEnabled")
class TSWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs), UIController, LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(context as LifecycleOwner)
    private var fullScreenView: ViewGroup? = null
    private var origOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private val downloadHandler = DownloadHandler(context)
    var dataListener: WebDataListener? = null

    val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                handleLongPress(e)
            }
        })

    init {
        setWebContentsDebuggingEnabled(true)
    }

    init {
        isFocusableInTouchMode = true
        isFocusable = true
        isHorizontalScrollBarEnabled = true
        isVerticalScrollBarEnabled = true
        scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        isScrollbarFadingEnabled = true
        isScrollContainer = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        }
        isSaveEnabled = true

        setDownloadListener(downloadHandler)

        settings.apply {
            allowContentAccess = true
            allowFileAccess = true
            builtInZoomControls = true
            databaseEnabled = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            javaScriptEnabled = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            setSupportZoom(true)

            setAppCachePath(context.cacheDir.path)
            setGeolocationEnabled(true)
            setGeolocationDatabasePath(File(context.filesDir, "geodb").path)
            setSupportMultipleWindows(true)
            addJavascriptInterface(TSBridge(this@TSWebView), "TSBridge")

            userAgentString = Settings.userAgent.value
        }

        setDarkMode(Settings.darkMode)
        setIncognito(Settings.incognito)

        webChromeClient = TSChromeClient(this)
        webViewClient = TSWebClient(this)



        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    init {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptThirdPartyCookies(this, Settings.acceptThirdPartyCookies)
    }

    fun setDarkMode(dark: Boolean) {
        if (dark) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
            }
        }
    }

    fun setIncognito(value: Boolean) {
        if (value) {
            CookieManager.getInstance().setAcceptCookie(false)
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.setAppCacheEnabled(false)
            clearHistory()
            clearCache(true)
            clearFormData()
            settings.savePassword = false
            settings.saveFormData = false
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.setAppCacheEnabled(true)
            settings.savePassword = true
            settings.saveFormData = true
        }
    }

    override fun loadUrl(url: String) {
        if (Settings.dnt) {
            super.loadUrl(url, mapOf("DNT" to "1"))
        } else {
            super.loadUrl(url)
        }
    }

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) {
        if (Settings.dnt) {
            additionalHttpHeaders["DNT"] = "1"
        }
        super.loadUrl(url, additionalHttpHeaders)
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        super.loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause() {
        super.onPause()
        evaluateJavascript("if(window.localStream){window.localStream.stop();}", null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun onDestroy() {
        val parent = parent as? ViewGroup
        parent?.removeView(this)
        stopLoading()
        onPause()
        removeAllViews()
        if (Settings.incognito) {
            clearHistory()
        }
        destroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun handleLongPress(event: MotionEvent) {
        val type = hitTestResult.type
        val extra = hitTestResult.extra
        logD("webview hitResult", type, extra)
        dataListener?.longPressState?.value =
            LongPressInfo(true, event.x.toInt(), event.y.toInt(), type, extra ?: "")
    }

    fun generatePreview() {
        if (width == 0 || height == 0) {
            return
        }
        val currentUrl = url
        ioScope.launch {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            if (currentUrl == "about:blank") {
                val window = (context as? Activity)?.window ?: return@launch
                val insets = ViewCompat.getRootWindowInsets(window.decorView)
                val height = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
                canvas.translate(0f, -1f * height)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val windowBitmap = Bitmap.createBitmap(window.decorView.width, window.decorView.height, Bitmap.Config.RGB_565)
                    PixelCopy.request(window, windowBitmap, {}, handler)
                    canvas.drawBitmap(windowBitmap, 0f, 0f, null)
                } else {
                    canvas.drawBitmap(window.decorView.drawingCache, 0f, 0f, null)
//                    window.decorView.draw(canvas)
                }
            } else {
                canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())
                draw(canvas)
            }
            dataListener?.previewState?.value = bitmap
            dataListener?.updateInfo()
        }
    }

    override fun onProgressChanged(progress: Int) {
        dataListener?.progressState?.value = progress * 1.0f / 100
    }

    override fun onReceivedTitle(title: String?) {
        dataListener?.titleState?.value = if (title == "about:blank") context.getString(R.string.new_tab) else title ?: ""
    }

    override fun onReceivedIcon(icon: Bitmap?) {
        dataListener?.onReceivedIcon(icon)
    }

    override fun onShowCustomView(
        view: View,
        requestedOrientation: Int,
        callback: WebChromeClient.CustomViewCallback
    ) {
        origOrientation = requestedOrientation
        post {
            val resolver = context.contentResolver
            val autoRotationOff = android.provider.Settings.System.getInt(
                resolver,
                android.provider.Settings.System.ACCELEROMETER_ROTATION
            ) == 0
            if (autoRotationOff && activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
        view.removeFromParent()
        if (fullScreenView != null) {
            callback.onCustomViewHidden()
        }

        val parentView = (context as? MainActivity)?.videoLayout
        parentView?.removeAllViews()
        parentView?.addView(view, FrameLayout.LayoutParams(-1, -1))
        parentView?.isVisible = true
        try {
            parentView?.keepScreenOn = true
        } catch (e: Exception) {
        }
        parentView?.setFullScreen(true)
        fullScreenView = parentView
    }

    override fun onHideCustomView() {
        try {
            fullScreenView?.keepScreenOn = false
        } catch (e: Exception) {
        }
        fullScreenView?.setFullScreen(false)
        fullScreenView?.isVisible = false
        fullScreenView?.removeAllViews()
        if (activity?.requestedOrientation != origOrientation) {
            activity?.requestedOrientation = origOrientation
        }
        fullScreenView = null
    }

    override fun onCreateWindow(resultMsg: Message): Boolean {
        dataListener?.onCreateWindow(resultMsg)
        return true
    }

    override fun onCloseWindow() {
        dataListener?.onCloseWindow()
    }

    override suspend fun requestPermissions(vararg permissions: String): Map<String, Boolean> {
        val activity = context as? BaseActivity
        return activity?.requestPermissions(*permissions) ?: mapOf()
    }

    override suspend fun showFileChooser(fileChooserParams: WebChromeClient.FileChooserParams): Array<Uri>? {
        val intent = fileChooserParams.createIntent()
        val activity = context as? BaseActivity ?: return null
        return suspendCancellableCoroutine { continuation ->
            activity.startActivityForResult(intent) { resultCode, data ->
                val uris = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                continuation.resume(uris)
            }
        }
    }

    override fun onPageStarted(url: String, favicon: Bitmap?) {
    }

    override fun onPageFinished(url: String) {
        evaluateJavascript(bridgeJs, null)
    }

    override fun doUpdateVisitedHistory(url: String, isReload: Boolean) {
        dataListener?.doUpdateVisitedHistory(url, isReload)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    companion object {
        val bridgeJs: String by lazy {
            App.instance.assets.open("tsbridge.js").use {
                BufferedReader(InputStreamReader(it)).readText()
            }
        }
    }
}