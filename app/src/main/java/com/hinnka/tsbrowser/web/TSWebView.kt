package com.hinnka.tsbrowser.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.core.view.children
import androidx.lifecycle.*
import com.hinnka.tsbrowser.persist.AppDatabase
import com.hinnka.tsbrowser.persist.SearchHistory
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.ext.*
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.home.LongPressInfo
import com.hinnka.tsbrowser.util.IconCache
import com.hinnka.tsbrowser.util.Settings
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

@SuppressLint("SetJavaScriptEnabled")
class TSWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs), UIController, LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(context as LifecycleOwner)
    private var fullScreenView: View? = null
    private var origOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private val downloadHandler = DownloadHandler(context)
    var dataListener: WebDataListener? = null

    val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                handleLongPress(e)
            }
        })

    var isWindow = false
    private val faviconMap = mutableMapOf<String, Bitmap?>()

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
            javaScriptCanOpenWindowsAutomatically = true
            javaScriptEnabled = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            setSupportZoom(true)

            setAppCacheEnabled(true)
            setAppCachePath(context.cacheDir.path)
            setGeolocationEnabled(true)
            setGeolocationDatabasePath(File(context.filesDir, "geodb").path)
            setSupportMultipleWindows(true)
        }

        webChromeClient = TSChromeClient(this)
        webViewClient = TSWebClient(this)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    init {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptThirdPartyCookies(this, true)
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
        ioScope.launch {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())
            draw(canvas)
            dataListener?.previewState?.value = bitmap
            dataListener?.updateInfo()
        }
    }

    override fun onProgressChanged(progress: Int) {
        dataListener?.progressState?.value = progress * 1.0f / 100
    }

    override fun onReceivedTitle(title: String?) {
        dataListener?.titleState?.value = title ?: url ?: ""
    }

    override fun onReceivedIcon(icon: Bitmap?) {
        url?.let { url ->
            url.host?.let {
                faviconMap[it] = icon
            }
            dataListener?.iconState?.value = icon

            lifecycleScope.launchWhenCreated {
                val search = SearchHistory(
                    this@TSWebView.originalUrl ?: "",
                    System.currentTimeMillis()
                )
                search.title = dataListener?.titleState?.value
                icon?.let {
                    url.host?.let { host ->
                        IconCache.save(host, it)
                    }
                }
                search.url = url
                val dao = AppDatabase.instance.searchHistoryDao()
                if (dao.getByName(search.query) != null) {
                    dao.update(search)
                }
            }
        }
    }

    override fun onShowCustomView(
        view: View,
        requestedOrientation: Int,
        callback: WebChromeClient.CustomViewCallback
    ) {
//        printView(view)
        origOrientation =
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        if (fullScreenView != null) {
            callback.onCustomViewHidden()
        }
        if (requestedOrientation != activity?.requestedOrientation) {
            activity?.requestedOrientation = requestedOrientation
        }
        val resolver = context.contentResolver
        val autoRotationOff = android.provider.Settings.System.getInt(
            resolver,
            android.provider.Settings.System.ACCELEROMETER_ROTATION
        ) == 0
        if (autoRotationOff && requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        fullScreenView = view
        val decorView = activity?.window?.decorView as? ViewGroup
        decorView?.addView(view, FrameLayout.LayoutParams(-1, -1))
        try {
            view.keepScreenOn = true
        } catch (e: Exception) {
        }
        view.setFullScreen(true)
        val videoView = view.findVideo
        videoView?.setOnErrorListener { _, _, _ -> false }
        videoView?.setOnCompletionListener {
            callback.onCustomViewHidden()
        }
    }

    private fun printView(view: View, root: Boolean = true) {
        if (root) {
            logD("=====printView=====")
        }
        logD(view.javaClass.name)
        if (view is ViewGroup) {
            for (child in view.children) {
                printView(child, false)
            }
        }
        if (root) {
            logD("=====printView=====")
        }
    }

    override fun onHideCustomView() {
        try {
            fullScreenView?.keepScreenOn = false
        } catch (e: Exception) {
        }
        fullScreenView?.setFullScreen(false)
        fullScreenView?.removeFromParent()
        if (activity?.requestedOrientation != origOrientation) {
            activity?.requestedOrientation = origOrientation
        }
        val videoView = fullScreenView?.findVideo
        videoView?.stopPlayback()
        videoView?.setOnErrorListener(null)
        videoView?.setOnCompletionListener(null)
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
    }

    override fun doUpdateVisitedHistory(url: String, isReload: Boolean) {
        dataListener?.urlState?.value = url
        url.host?.let { host ->
            lifecycleScope.launchWhenCreated {
                if (faviconMap[host] == null) {
                    faviconMap[host] = IconCache.asyncGet(host)
                }
                dataListener?.iconState?.value = faviconMap[host]
            }
        }
        dataListener?.canGoBackState?.value = canGoBack()
        dataListener?.canGoForwardState?.value = canGoForward()
        generatePreview()
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    private val View.findVideo: VideoView?
        get() {
            if (this is VideoView) {
                return this
            } else if (this is ViewGroup) {
                return this.children.firstOrNull { it is VideoView } as? VideoView
            }
            return null
        }
}