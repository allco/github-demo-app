package se.allco.githubbrowser.common.ui

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class LambdaWebViewClient constructor(
    private val onLoadStarted: ((url: String) -> Unit)? = null,
    private val onLoadFinished: ((url: String) -> Unit)? = null,
    private val overrideUrlLoading: ((uri: Uri) -> Boolean)? = null, // should return `true` to cancel the loading
    private val onLoadError: (() -> Unit)? = null,
) : WebViewClient() {

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onLoadStarted?.invoke(url)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onLoadFinished?.invoke(url)
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        super.onReceivedError(view, request, error)
        onLoadError?.invoke()
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        super.shouldOverrideUrlLoading(view, url)
        return overrideUrlLoading?.invoke(Uri.parse(url)) ?: false
    }
}
