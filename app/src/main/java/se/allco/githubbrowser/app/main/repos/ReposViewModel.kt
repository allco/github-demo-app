package se.allco.githubbrowser.app.main.repos

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.common.ui.addSmartLoadingState
import se.allco.githubbrowser.common.utils.getString
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber
import javax.inject.Inject

class ReposViewModel @Inject constructor(
    application: Application,
    reposRepository: ReposRepository,
    private val reposItemViewModelFactory: ReposItemViewModel.Factory,
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    private sealed class State {
        object Initializing : State()
        object Loading : State()
        class Content(val data: List<ReposItemViewModel>) : State()
        class Error(@StringRes val messageRes: Int, val allowRetry: Boolean = true) : State()
    }

    val showLoading = MutableLiveData(false)
    val showContent = MutableLiveData(false)
    val errorMessage = MutableLiveData<String>(null)
    val showError: LiveData<Boolean> = errorMessage.map { it != null }

    private val retrySubject = BehaviorSubject.createDefault(Unit)

    val listItems: LiveData<List<ReposItemViewModel>> =
        retrySubject
            .switchMap {

                reposRepository
                    .getRepos()
                    .map { listRepos ->
                        onDataReceived(listRepos)
                    }
                    .addSmartLoadingState { State.Loading }
                    .doOnSubscribe { renderState(State.Initializing) }
                    .doOnNext(::renderState)
                    .ofType(State.Content::class.java)
                    .map { it.data }
                    .onErrorResumeNext(::createErrorHandler)

            }
            .toLiveData()

    private fun onDataReceived(listRepos: List<ReposRepository.Repo>): State {
        return when {
            listRepos.isEmpty() -> State.Error(
                R.string.main_repos_no_repos_found,
                allowRetry = false,
            )
            else -> State.Content(
                data = listRepos.map { reposItemViewModelFactory.create(it) },
            )
        }
    }

    private fun createErrorHandler(err: Throwable): Observable<List<ReposItemViewModel>> {
        Timber.w(err, "ReposViewModel failed")
        return Observable
            .never<List<ReposItemViewModel>>()
            .doOnSubscribe { renderState(State.Error(R.string.main_repos_error_fetching_data)) }
    }

    private fun renderState(state: State) {
        Timber.v("renderState() $state")
        when (state) {
            State.Initializing -> {
                showContent.postValue(false)
                showLoading.postValue(false)
                errorMessage.postValue(null)
            }
            State.Loading -> {
                showContent.postValue(false)
                errorMessage.postValue(null)
                showLoading.postValue(true)
            }
            is State.Content -> {
                showLoading.postValue(false)
                errorMessage.postValue(null)
                showContent.postValue(true)
            }
            is State.Error -> {
                showContent.postValue(false)
                showLoading.postValue(false)
                errorMessage.postValue(getString(state.messageRes))
            }
        }
    }

    fun onRetryClicked() {
        retrySubject.onNext(Unit)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
