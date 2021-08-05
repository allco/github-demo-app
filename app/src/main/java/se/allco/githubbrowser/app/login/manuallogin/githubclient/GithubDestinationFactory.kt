package se.allco.githubbrowser.app.login.manuallogin.githubclient

import android.net.Uri
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewDestination
import javax.inject.Inject

class GithubDestinationFactory @Inject constructor() {
    fun create(requestId: String): WebViewDestination =
        WebViewDestination(
            url = Uri.Builder().apply {
                scheme("https")
                    .authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("state", requestId)
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
            }.build().toString(),
            headers = mapOf("Accept" to "application/vnd.github.machine-man-preview+json")
        )
}
