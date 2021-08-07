package se.allco.githubbrowser.app.login.manuallogin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.login.manuallogin.githubclient.GithubWebViewModel
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.ui.LoadableContentViewModel
import se.allco.githubbrowser.common.utils.getString
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber
import javax.inject.Inject

class ManualLoginViewModel @Inject constructor(
    application: Application,
    val contentViewModel: LoadableContentViewModel,
    val githubViewModel: GithubWebViewModel,
    private val model: ManualLoginModel,
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    val authenticatedUser =
        waitForGithubCode()
            .concatMap(::authenticateWithCode)
            .onErrorResumeNext(::createErrorHandler)
            .toLiveData(disposables)

    private fun waitForGithubCode(): Single<GithubCode> =
        githubViewModel
            .states
            .doOnNext(::onGithubWebViewEvent)
            .ofType(GithubWebViewModel.Event.GithubCodeReceived::class.java)
            .map { it.code }
            .firstOrError()

    private fun onGithubWebViewEvent(it: GithubWebViewModel.Event?) {
        when (it) {
            is GithubWebViewModel.Event.GithubCodeReceived ->
                contentViewModel.renderStateLoading()
            is GithubWebViewModel.Event.PageLoadingError ->
                contentViewModel.renderStateError(getString(it.messageRes), allowRetry = true)
            GithubWebViewModel.Event.PageLoadingStarted ->
                contentViewModel.renderStateLoading()
            GithubWebViewModel.Event.PageLoadingSuccess ->
                contentViewModel.renderStateContentReady()
        }
    }

    private fun authenticateWithCode(githubCode: GithubCode): Single<User.Valid> =
        model.authenticateWithCode(githubCode)

    private fun createErrorHandler(err: Throwable): Single<User.Valid> {
        Timber.e(err, "ManualLoginViewModel failed")
        return model
            .createErrorHandler()
            .doOnSubscribe { renderErrorFetchingUserData() }
            .andThen(Single.never())
    }

    private fun renderErrorFetchingUserData() {
        val message = getString(R.string.login_manual_error_user_data_fetching)
        contentViewModel.renderStateError(message, allowRetry = true)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
