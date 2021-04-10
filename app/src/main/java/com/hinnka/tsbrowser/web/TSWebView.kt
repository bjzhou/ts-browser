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
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.ext.activity
import com.hinnka.tsbrowser.ext.ioScope
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.ext.setFullScreen
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.util.Settings
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.min

@SuppressLint("SetJavaScriptEnabled")
class TSWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs), UIController, LifecycleOwner {

    override val userLinks = mutableSetOf<String>()

    private val lifecycleRegistry = LifecycleRegistry(context as LifecycleOwner)
    private var fullScreenView: View? = null
    private var origOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private val downloadHandler = DownloadHandler(context)

    var isWindow = false

    val urlState = MutableLiveData("")
    val progressState = MutableLiveData(0f)
    val titleState = MutableLiveData("")
    val iconState = MutableLiveData<Bitmap?>()
    val previewState = MutableLiveData<Bitmap?>()

    var onCreateWindow: (Message) -> Unit = {}
    var onCloseWindow: () -> Unit = {}

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
        lifecycleScope.launchWhenResumed {
            urlState.postValue(url)
        }
    }

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) {
        if (Settings.dnt) {
            additionalHttpHeaders["DNT"] = "1"
        }
        super.loadUrl(url, additionalHttpHeaders)
        lifecycleScope.launchWhenResumed {
            urlState.postValue(url)
        }
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        super.loadData(data, mimeType, encoding)
        lifecycleScope.launchWhenResumed {
            urlState.postValue(data.substring(0, min(10, data.length)))
        }
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
        lifecycleScope.launchWhenResumed {
            urlState.postValue(baseUrl ?: "")
        }
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

    fun generatePreview() {
        if (width == 0 || height == 0) {
            return
        }
        ioScope.launch {
            val bitmap = Bitmap.createBitmap(width / 2, height / 2, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            canvas.scale(0.5f, 0.5f)
            draw(canvas)
            previewState.postValue(bitmap)
        }
    }

    override fun onProgressChanged(progress: Int) {
        lifecycleScope.launchWhenResumed {
            progressState.postValue(progress * 1f / 100)
        }
    }

    override fun onReceivedTitle(title: String?) {
        lifecycleScope.launchWhenResumed {
            titleState.postValue(title ?: urlState.value)
        }
    }

    override fun onReceivedIcon(icon: Bitmap?) {
        lifecycleScope.launchWhenResumed {
            iconState.postValue(icon)
        }
    }

    override fun onShowCustomView(
        view: View,
        requestedOrientation: Int,
        callback: WebChromeClient.CustomViewCallback
    ) {
        printView(view)
        origOrientation =
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        if (fullScreenView != null) {
            callback.onCustomViewHidden()
        }
        if (requestedOrientation != activity?.requestedOrientation) {
            activity?.requestedOrientation = requestedOrientation
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
        println("TSBrowser onShowCustomView")
    }

    private fun printView(view: View, root: Boolean = true) {
        if (root) {
            println("TSBrowser: =====printView=====")
        }
        println("TSBrowser: ${view.javaClass.name}")
        if (view is ViewGroup) {
            for (child in view.children) {
                printView(child, false)
            }
        }
        if (root) {
            println("TSBrowser: =====printView=====")
        }
    }

    override fun onHideCustomView() {
        println("TSBrowser onHideCustomView")
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
        println("TSBrowser onHideCustomView")
    }

    override fun onCreateWindow(resultMsg: Message): Boolean {
        onCreateWindow.invoke(resultMsg)
        return true
    }

    override fun onCloseWindow() {
        onCloseWindow.invoke()
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
        urlState.postValue(url)
        favicon?.let {
            iconState.postValue(it)
        }

        val hit = hitTestResult

        if (!isWindow && userLinks.isEmpty()) {
            userLinks.add(url)
        }

        if (hit.type > 0) {
            userLinks.add(url)
        }

        println("TSBrowser onPageStarted $url")
        println("TSBrowser in backList ${copyBackForwardList().currentItem?.url}")
    }

    override fun onPageFinished(url: String) {
        urlState.postValue(url)

        val hit = hitTestResult
        println("TSBrowser onPageFinished $url")
        println("TSBrowser in backList ${copyBackForwardList().currentItem?.url}")

        if (hit.type > 0) {
            userLinks.add(url)
        }
    }

    override fun canGoBack(): Boolean {
        val list = copyBackForwardList()
        val current = list.currentIndex
        if (current == 0) {
            return false
        }
        for (index in 0 until current) {
            val item = list.getItemAtIndex(index)
            if (userLinks.contains(item.url) || userLinks.contains(item.originalUrl)) {
                return true
            }
        }
        return false
    }

    override fun goBack() {
        val list = copyBackForwardList()
        val current = list.currentIndex
        for (index in current-1 downTo 0) {
            val item = list.getItemAtIndex(index)
            if (userLinks.contains(item.url) || userLinks.contains(item.originalUrl)) {
                goBackOrForward(index - current)
                return
            }
        }
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