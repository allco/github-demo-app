package se.allco.githubbrowser.app.login.manuallogin

import android.app.Application
import io.reactivex.rxjava3.core.Single
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.login.manuallogin.github_client.GithubWebClient
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.ui.LoadableContentViewModel
import timber.log.Timber
import javax.inject.Inject

class ManualLoginViewModel @Inject constructor(
    application: Application,
    val webClient: GithubWebClient,
    private val model: ManualLoginModel,
) : LoadableContentViewModel<User.Valid>(application) {

    companion object {
        private fun GithubWebClient.Event.asViewModelState(): LoadableContentViewModel.State =
            when (this) {
                GithubWebClient.Event.PageLoadingStarted -> State.Loading
                GithubWebClient.Event.PageLoadingSuccess -> State.ShowContent
                is GithubWebClient.Event.PageLoadingError -> State.Error(messageRes)
                is GithubWebClient.Event.GithubCodeReceived -> State.Loading
            }
    }

    override fun loadContent(): Single<User.Valid> {
        return waitForGithubCode()
            .concatMap(::authenticateWithCode)
            .onErrorResumeNext(::createErrorHandler)
    }

    private fun waitForGithubCode(): Single<GithubCode> =
        webClient
            .states
            .takeUntil { it is GithubWebClient.Event.GithubCodeReceived }
            .doOnNext { it.asViewModelState().let(::renderState) }
            .ofType(GithubWebClient.Event.GithubCodeReceived::class.java)
            .map { it.code }
            .firstOrError()

    private fun authenticateWithCode(githubCode: GithubCode): Single<User.Valid> =
        model.authenticateWithCode(githubCode)

    private fun createErrorHandler(err: Throwable): Single<User.Valid> {
        Timber.w(err, "ManualLoginViewModel failed")
        return model
            .createErrorHandler()
            .doOnSubscribe { renderState(State.Error(R.string.login_manual_error_user_data_fetching)) }
            .andThen(Single.never())
    }
}
