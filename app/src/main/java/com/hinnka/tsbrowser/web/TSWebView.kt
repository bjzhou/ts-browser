package com.hinnka.tsbrowser.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.ext.activity
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.ext.setFullScreen
import com.hinnka.tsbrowser.ui.base.BaseActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.min

@SuppressLint("SetJavaScriptEnabled")
class TSWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs), UIController, LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(context as LifecycleOwner)
    private var fullScreenView: View? = null
    private var origOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private val downloadHandler = DownloadHandler(context)

    val urlState = MutableStateFlow("")
    val progressState = MutableStateFlow(0)
    val titleState = MutableStateFlow("")
    val iconState = MutableStateFlow<Bitmap?>(null)

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
        super.loadUrl(url)
        lifecycleScope.launchWhenResumed {
            urlState.emit(url)
        }
    }

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) {
        super.loadUrl(url, additionalHttpHeaders)
        lifecycleScope.launchWhenResumed {
            urlState.emit(url)
        }
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        super.loadData(data, mimeType, encoding)
        lifecycleScope.launchWhenResumed {
            urlState.emit(data.substring(0, min(10, data.length)))
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
            urlState.emit(baseUrl ?: "")
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

    override fun onProgressChanged(progress: Int) {
        lifecycleScope.launchWhenResumed {
            progressState.emit(progress)
        }
    }

    override fun onReceivedTitle(title: String?) {
        lifecycleScope.launchWhenResumed {
            titleState.emit(title ?: urlState.value)
        }
    }

    override fun onReceivedIcon(icon: Bitmap?) {
        lifecycleScope.launchWhenResumed {
            iconState.emit(icon)
        }
    }

    override fun onShowCustomView(view: View, requestedOrientation: Int, callback: WebChromeClient.CustomViewCallback) {
        origOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
        lifecycleScope.launchWhenResumed {
            favicon?.let {
                iconState.emit(it)
            }
        }
    }

    override fun onPageFinished(url: String) {
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