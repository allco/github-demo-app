package se.allco.githubbrowser.common.ui.databinding.webview

import android.webkit.WebView
import androidx.databinding.BindingAdapter

@BindingAdapter("webViewDestination")
fun setWebViewDestination(view: WebView, destination: WebViewDestination?) {
    if (destination == null) return
    view.loadUrl(destination.url, destination.headers)
}

@BindingAdapter("webViewSettings")
fun setWebViewSettings(view: WebView, settings: WebViewSettings?) {
    settings?.apply {
        view.settings.javaScriptEnabled = javaScriptEnabled
    }
}
