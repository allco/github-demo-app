package se.allco.githubbrowser.app.login.manuallogin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.login.manuallogin.githubclient.GithubWebViewModel
import se.allco.githubbrowser.app.login.manuallogin.githubclient.GithubWebViewModel.Event.GithubCodeReceived
import se.allco.githubbrowser.app.login.manuallogin.githubclient.GithubWebViewModel.Event.PageLoadingError
import se.allco.githubbrowser.app.login.manuallogin.githubclient.GithubWebViewModel.Event.PageLoadingStarted
import se.allco.githubbrowser.app.login.manuallogin.githubclient.GithubWebViewModel.Event.PageLoadingSuccess
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.ui.LoadableContentViewModel
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber

class ManualLoginViewModel @Inject constructor(
    application: Application,
    val contentViewModel: LoadableContentViewModel,
    val githubViewModel: GithubWebViewModel,
    private val model: ManualLoginModel,
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    val authenticatedUser = waitForAuthenticatedUser().toLiveData(disposables)

    private fun waitForAuthenticatedUser(): Observable<User.Valid> =
        contentViewModel.runRetryable {
            githubViewModel
                .states
                .renderStates()
                .unwrapCodeFromState()
                .firstOrError()
                .flatMap(model::authenticateWithCode)
                .onErrorResumeNext(::createErrorHandler)
        }

    private fun Observable<GithubWebViewModel.Event>.renderStates() =
        doOnSubscribe { contentViewModel.renderInitialisation() }
            .doOnNext { event ->
                when (event) {
                    PageLoadingStarted -> contentViewModel.renderStateLoading()
                    PageLoadingSuccess -> contentViewModel.renderStateContentReady()
                    is GithubCodeReceived -> contentViewModel.renderStateLoading()
                    is PageLoadingError -> contentViewModel.renderStateError(event.messageRes, true)
                }
            }

    private fun Observable<GithubWebViewModel.Event>.unwrapCodeFromState() =
        ofType(GithubCodeReceived::class.java)
            .map { it.code }

    private fun createErrorHandler(err: Throwable): Single<User.Valid> {
        Timber.e(err, "ManualLoginViewModel failed")
        return model
            .createErrorHandler()
            .doOnSubscribe { renderErrorFetchingUserData() }
            .andThen(Single.never())
    }

    private fun renderErrorFetchingUserData() {
        contentViewModel.renderStateError(
            R.string.login_manual_error_user_data_fetching,
            allowRetry = true,
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
