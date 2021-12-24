package se.allco.githubbrowser.app.main.repos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.utils.toLiveData
import se.allco.githubbrowser.utils.ui.LoadableContentViewModel
import se.allco.githubbrowser.utils.ui.attachSmartLoading
import timber.log.Timber

class ReposViewModel @Inject constructor(
    application: Application,
    val contentViewModel: LoadableContentViewModel,
    private val reposRepository: ReposRepository,
    private val reposItemViewModelFactory: ReposItemViewModel.Factory,
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    val listItems: LiveData<List<ReposItemViewModel>> = waitForItems().toLiveData()

    private fun waitForItems() =
        contentViewModel.runRetryable {
            reposRepository
                .getRepos()
                .unwrapItems()
                .renderStates()
                .onErrorResumeNext(::createErrorHandler)
        }

    private fun Single<List<ReposRepository.Repo>>.unwrapItems(): Single<List<ReposItemViewModel>> =
        map(reposItemViewModelFactory::create)

    private fun Single<List<ReposItemViewModel>>.renderStates() =
        doOnSubscribe { contentViewModel.renderInitialisation() }
            .attachSmartLoading { onShowLoading = contentViewModel::renderStateLoading }
            .doOnSuccess(::renderStateContent)

    private fun renderStateContent(list: List<ReposItemViewModel>) =
        when {
            list.isEmpty() -> renderStateEmptyList()
            else -> contentViewModel.renderStateContentReady()
        }

    private fun createErrorHandler(err: Throwable): Single<List<ReposItemViewModel>> {
        Timber.w(err, "ReposViewModel failed")
        return Single
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
