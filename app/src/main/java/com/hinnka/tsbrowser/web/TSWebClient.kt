package com.hinnka.tsbrowser.web

import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.adblock.AdBlocker
import java.util.concurrent.TimeUnit

class TSWebClient(private val controller: UIController) : WebViewClient() {

    companion object {
        val localSchemes = arrayOf("http", "https", "ftp", "file", "about", "chrome", "data", "javascript")
        val sslKeepLastDuration = TimeUnit.SECONDS.toMillis(15)
    }

    private var sslLastAllow = false
    private var sslLastTime = 0L

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        if (localSchemes.contains(uri.scheme)) {
            view.loadUrl(uri.toString())
            return true
        }
        return try {
            val intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
            val componentName = intent.resolveActivity(view.context.packageManager)
            if (componentName == null) {
                val packageName = intent.`package`
                if (!TextUtils.isEmpty(packageName)) {
                    val marketIntent = Intent.parseUri("market://search?q=pname:$packageName", Intent.URI_INTENT_SCHEME)
                    try {
                        view.context.startActivity(marketIntent)
                    } catch (e: Exception) {
                    }
                }
            } else {
                try {
                    view.context.startActivity(intent)
                } catch (e: Exception) {
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        controller.onPageStarted(url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        controller.onPageFinished(url)
    }

    override fun onLoadResource(view: WebView, url: String) {
        super.onLoadResource(view, url)
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        if (AdBlocker.shouldBlock(request.url)) {
            return AdBlocker.emptyResponse
        }
        return null
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("TSWebView", "error ${error.errorCode}: ${error.description}")
        }
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        Log.e("TSWebView", "http error ${errorResponse.statusCode}: ${errorResponse.reasonPhrase}")
    }

    override fun onFormResubmission(view: WebView, dontResend: Message, resend: Message) {
        AlertDialog.Builder(view.context).apply {
            setTitle(R.string.form_resubmission)
            setMessage(R.string.resend_data)
            setCancelable(false)
            setPositiveButton(android.R.string.yes) { _, _ ->
                resend.sendToTarget()
            }
            setNegativeButton(android.R.string.no) { _, _ ->
                dontResend.sendToTarget()
            }
        }.show()
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        if (System.currentTimeMillis() - sslLastTime <= sslKeepLastDuration) {
            if (sslLastAllow) {
                handler.proceed()
            } else {
                handler.cancel()
            }
            return
        }
        AlertDialog.Builder(view.context).apply {
            setTitle(R.string.warning)
            setMessage(R.string.insecue_message)
            setPositiveButton(android.R.string.yes) { _, _ ->
                sslLastAllow = true
                sslLastTime = System.currentTimeMillis()
                handler.proceed()
            }
            setNegativeButton(android.R.string.no) { _, _ ->
                sslLastAllow = false
                sslLastTime = System.currentTimeMillis()
                handler.cancel()
            }
        }.show()
    }

    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
        super.onReceivedClientCertRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView,
        handler: HttpAuthHandler,
        host: String,
        realm: String
    ) {
        var user = ""
        var password = ""
        AlertDialog.Builder(view.context).apply {
            setView(ComposeView(view.context).apply { 
                setContent { 
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp)) {
                        Text(text = realm, maxLines = 3)
                        OutlinedTextField(value = "", onValueChange = {
                            user = it
                        }, placeholder = { Text(text = stringResource(id = R.string.username)) })
                        OutlinedTextField(value = "", onValueChange = {
                            password = it
                        }, placeholder = { Text(text = stringResource(id = R.string.password)) })
                    }
                }
            })
            setTitle(R.string.signin)
            setPositiveButton(android.R.string.ok) { _, _ ->
                handler.proceed(user, password)
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                handler.cancel()
            }
        }.show()
    }

    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        return super.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
        super.onUnhandledKeyEvent(view, event)
    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        super.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(
        view: WebView?,
        realm: String?,
        account: String?,
        args: String?
    ) {
        super.onReceivedLoginRequest(view, realm, account, args)
    }

    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        return super.onRenderProcessGone(view, detail)
    }

    override fun onSafeBrowsingHit(
        view: WebView?,
        request: WebResourceRequest?,
        threatType: Int,
        callback: SafeBrowsingResponse?
    ) {
        super.onSafeBrowsingHit(view, request, threatType, callback)
    }
}