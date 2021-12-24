package se.allco.githubbrowser.app.login.manuallogin.githubclient

import android.net.Uri
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.UUID
import javax.inject.Inject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.login.manuallogin.GithubCode
import se.allco.githubbrowser.common.networkreporter.ConnectivityStateReporter
import se.allco.githubbrowser.common.ui.LambdaWebViewClient
import se.allco.githubbrowser.common.ui.attachSmartLoading
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewDestination
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewSettings
import se.allco.githubbrowser.common.utils.timeoutFirst

class GithubWebViewModel @Inject constructor(
    connectivityReporter: ConnectivityStateReporter,
    private val destinationFactory: GithubDestinationFactory,
    private val reader: GithubCodeParser,
) {
    sealed interface Event {
        companion object {
            val GenericError get() = PageLoadingError(R.string.login_manual_error_web_loading)
        }

        object PageLoadingStarted : Event
        object PageLoadingSuccess : Event
        data class GithubCodeReceived(val code: GithubCode) : Event
        data class PageLoadingError(@StringRes val messageRes: Int) : Event
    }

    // WebView Input
    val settings = WebViewSettings(javaScriptEnabled = true)
    val webClient = MutableLiveData<WebViewClient>()
    val destination = MutableLiveData<WebViewDestination>()

    // WebView Output
    val states: Observable<Event> =
        connectivityReporter
            .states()
            .switchMap(::onNetworkStateChanged)
            .onErrorReturn { Event.GenericError }

    private val stateSubject = PublishSubject.create<Event>().toSerialized()

    private fun onNetworkStateChanged(online: Boolean): Observable<Event> =
        if (online) onInternetAvailable() else onInternetUnavailable()

    private fun onInternetUnavailable(): Observable<Event> =
        Observable.just(Event.PageLoadingError(R.string.login_manual_error_no_network_connection))

    private fun onInternetAvailable(): Observable<Event> =
        stateSubject
            .attachSmartLoading { onShowLoadingEmitter = { Event.PageLoadingStarted } }
            .doOnSubscribe { loadAuthenticationPage() }
            .timeoutFirst(timeMs = 2000)

    private fun loadAuthenticationPage() {
        val requestId = UUID.randomUUID().toString()
        val destination = destinationFactory.create(requestId)
        this.destination.postValue(destination)
        this.webClient.postValue(createWebViewClient(requestId))
    }

    private fun createWebViewClient(requestId: String) =
        LambdaWebViewClient(
            overrideUrlLoading = { uri -> overrideUrlLoading(uri, requestId) },
            onLoadFinished = { stateSubject.onNext(Event.PageLoadingSuccess) },
            onLoadError = { stateSubject.onNext(Event.PageLoadingError(R.string.login_manual_error_web_loading)) }
        )

    private fun overrideUrlLoading(uri: Uri, requestId: String): Boolean {
        val code = reader.tryToParse(uri, requestId) ?: return false
        onCodeReceived(code)
        return true
    }

    private fun onCodeReceived(code: GithubCode) {
        stateSubject.onNext(Event.GithubCodeReceived(code))
    }
}
