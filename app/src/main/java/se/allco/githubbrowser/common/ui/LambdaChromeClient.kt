package se.allco.githubbrowser.common.ui

import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import timber.log.Timber

class LambdaChromeClient(
    val onJsAlert: (result: JsResult) -> Boolean = { it.cancel().let { true } },
    val onJsConfirm: (result: JsResult) -> Boolean = { it.cancel().let { true } },
) : WebChromeClient() {

    override fun onJsAlert(
        view: WebView,
        url: String,
        message: String,
        result: JsResult,
    ): Boolean {
        Timber.e(message)
        return onJsAlert(result) // return true to eat all alert messages
    }

    override fun onJsConfirm(
        view: WebView,
        url: String,
        message: String,
        result: JsResult,
    ): Boolean {
        return onJsConfirm(result) // return true to eat all confirm dialogs
    }
}
