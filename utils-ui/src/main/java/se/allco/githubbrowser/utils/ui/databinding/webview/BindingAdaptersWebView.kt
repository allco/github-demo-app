package se.allco.githubbrowser.common.ui.databinding.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.databinding.BindingAdapter

@BindingAdapter("destination")
fun setWebViewDestination(view: WebView, destination: WebViewDestination?) {
    if (destination == null) return
    view.loadUrl(destination.url, destination.headers)
}

@SuppressLint("SetJavaScriptEnabled")
@BindingAdapter("settings")
fun setWebViewSettings(view: WebView, settings: WebViewSettings?) {
    settings?.apply {
        view.settings.javaScriptEnabled = javaScriptEnabled
    }
}
