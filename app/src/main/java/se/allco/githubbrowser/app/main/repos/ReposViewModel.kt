package se.allco.githubbrowser.app.main.repos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import se.allco.githubbrowser.R
import se.allco.githubbrowser.common.ui.LoadableContentViewModel
import se.allco.githubbrowser.common.ui.attachSmartLoading
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber
import javax.inject.Inject

class ReposViewModel @Inject constructor(
    application: Application,
    reposRepository: ReposRepository,
    val contentViewModel: LoadableContentViewModel,
    private val reposItemViewModelFactory: ReposItemViewModel.Factory,
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    val listItems: LiveData<List<ReposItemViewModel>> =
        contentViewModel
            .runRetryable<List<ReposItemViewModel>> {
                reposRepository
                    .getRepos()

                    .map { list -> list.map { reposItemViewModelFactory.create(it) } }
                    .renderStates()
            }
            .onErrorResumeNext(::createErrorHandler)
            .toLiveData()

    private fun Single<List<ReposItemViewModel>>.renderStates() =
        doOnSubscribe { contentViewModel.renderInitialisation() }
            .attachSmartLoading { onShowLoading = { contentViewModel.renderStateLoading() } }
            .doOnSuccess { list ->
                when {
                    list.isEmpty() -> renderStateEmptyList()
                    else -> contentViewModel.renderStateContentReady()
                }
            }

    private fun createErrorHandler(err: Throwable): Observable<List<ReposItemViewModel>> {
        Timber.w(err, "ReposViewModel failed")
        return Observable
            .never<List<ReposItemViewModel>>()
            .doOnSubscribe { renderStateErrorFetchData() }
    }

    private fun renderStateEmptyList() {
        contentViewModel.renderStateError(R.string.main_repos_no_repos_found, false)
    }

    private fun renderStateErrorFetchData() {
        contentViewModel.renderStateError(R.string.main_repos_error_fetching_data, true)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
