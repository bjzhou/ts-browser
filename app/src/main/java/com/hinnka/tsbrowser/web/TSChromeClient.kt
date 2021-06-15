package com.hinnka.tsbrowser.web

import android.Manifest
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.activity
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.mainScope
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import kotlinx.coroutines.launch
import kotlin.math.min

class TSChromeClient(private val controller: UIController) : WebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        controller.onProgressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        controller.onReceivedTitle(title)
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        controller.onReceivedIcon(icon)
    }

    override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
        super.onReceivedTouchIconUrl(view, url, precomposed)
    }

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        controller.onShowCustomView(view, view.activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, callback)
    }

    override fun onShowCustomView(
        view: View,
        requestedOrientation: Int,
        callback: CustomViewCallback
    ) {
        controller.onShowCustomView(view, requestedOrientation, callback)
    }

    override fun onHideCustomView() {
        controller.onHideCustomView()
    }

    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message
    ): Boolean {
        return controller.onCreateWindow(resultMsg)
    }

    override fun onRequestFocus(view: WebView?) {
        super.onRequestFocus(view)
    }

    override fun onCloseWindow(window: WebView) {
        controller.onCloseWindow()
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return super.onJsAlert(view, url, message, result)
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return super.onJsConfirm(view, url, message, result)
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        return super.onJsPrompt(view, url, message, defaultValue, result)
    }

    override fun onJsBeforeUnload(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return super.onJsBeforeUnload(view, url, message, result)
    }

    override fun onExceededDatabaseQuota(
        url: String?,
        databaseIdentifier: String?,
        quota: Long,
        estimatedDatabaseSize: Long,
        totalQuota: Long,
        quotaUpdater: WebStorage.QuotaUpdater?
    ) {
        super.onExceededDatabaseQuota(
            url,
            databaseIdentifier,
            quota,
            estimatedDatabaseSize,
            totalQuota,
            quotaUpdater
        )
    }

    override fun onReachedMaxAppCacheSize(
        requiredStorage: Long,
        quota: Long,
        quotaUpdater: WebStorage.QuotaUpdater?
    ) {
        super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String,
        callback: GeolocationPermissions.Callback
    ) {
        logD("TSWebView onGeolocationPermissionsShowPrompt")
        val context = (controller as? View)?.context ?: run {
            callback.invoke(origin, false, true)
            return
        }
        AlertBottomSheet.Builder(context).apply {
            setTitle(R.string.location)
            setMessage(context.getString(R.string.location_message, "\"${origin.subSequence(0, min(origin.length, 50))}\""))
            setCancelable(false)
            setPositiveButton(android.R.string.ok) {
                mainScope.launch {
                    val allow = controller.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    callback.invoke(origin, allow, true)
                }
            }
            setNegativeButton(android.R.string.cancel) {
                callback.invoke(origin, false, true)
            }
        }.show()
    }

    override fun onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt()
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        mainScope.launch {
            val permissions = request.resources.map {
                when (it) {
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
                    else -> ""
                }
            }.filter { !TextUtils.isEmpty(it) }.toTypedArray()
            controller.requestPermissions(*permissions)
            request.grant(request.resources)
        }
    }

    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
        super.onPermissionRequestCanceled(request)
    }

    override fun onJsTimeout(): Boolean {
        return super.onJsTimeout()
    }

    override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
        super.onConsoleMessage(message, lineNumber, sourceID)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        val tag = "TSWebView"
        val msg = "${consoleMessage.sourceId()} in Line ${consoleMessage.lineNumber()}: ${consoleMessage.message()}"
        when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.WARNING -> Log.w(tag, msg)
            ConsoleMessage.MessageLevel.ERROR -> Log.e(tag, msg)
            ConsoleMessage.MessageLevel.DEBUG -> Log.d(tag, msg)
            else -> Log.i(tag, msg)
        }
        return true
    }

    override fun getDefaultVideoPoster(): Bitmap? {
        return null
    }

    override fun getVideoLoadingProgressView(): View {
        return ComposeView(App.instance).apply {
            setContent {
                CircularProgressIndicator(color = Color.Yellow)
            }
        }
    }

    override fun getVisitedHistory(callback: ValueCallback<Array<String>>?) {
        super.getVisitedHistory(callback)
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        mainScope.launch {
            val uris = controller.showFileChooser(fileChooserParams)
            filePathCallback.onReceiveValue(uris)
        }
        return true
    }
}