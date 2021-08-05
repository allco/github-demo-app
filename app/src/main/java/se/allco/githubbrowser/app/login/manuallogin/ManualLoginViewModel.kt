package se.allco.githubbrowser.app.login.manuallogin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.rxjava3.core.Single
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

    val result =
        waitForGithubCode()
            .concatMap(::authenticateWithCode)
            .onErrorResumeNext(::createErrorHandler)
            .toLiveData()

    private fun waitForGithubCode(): Single<GithubCode> =
        githubViewModel
            .states
            .doOnNext {
                when (it) {
                    is GithubWebViewModel.Event.GithubCodeReceived ->
                        contentViewModel.renderLoading()
                    is GithubWebViewModel.Event.PageLoadingError ->
                        contentViewModel.renderError(getString(it.messageRes), allowRetry = true)
                    GithubWebViewModel.Event.PageLoadingStarted ->
                        contentViewModel.renderLoading()
                    GithubWebViewModel.Event.PageLoadingSuccess ->
                        contentViewModel.renderContentReady()
                }
            }
            .takeUntil { it is GithubWebViewModel.Event.GithubCodeReceived }
            .ofType(GithubWebViewModel.Event.GithubCodeReceived::class.java)
            .map { it.code }
            .firstOrError()

    private fun authenticateWithCode(githubCode: GithubCode): Single<User.Valid> =
        model.authenticateWithCode(githubCode)

    private fun createErrorHandler(err: Throwable): Single<User.Valid> {
        Timber.e(err, "ManualLoginViewModel failed")
        return model
            .createErrorHandler()
            .doOnSubscribe {
                contentViewModel.renderError(
                    getString(R.string.login_manual_error_user_data_fetching),
                    allowRetry = true,
                )
            }
            .andThen(Single.never())
    }
}
