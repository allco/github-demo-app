package se.allco.githubbrowser.common.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import se.allco.githubbrowser.common.utils.getString
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber

abstract class LoadableContentViewModel<T> constructor(application: Application) :
    AndroidViewModel(application) {

    sealed class State {
        object Initializing : State()
        object Loading : State()
        object ShowContent : State()
        data class Error(@StringRes val messageRes: Int, val allowRetry: Boolean = false) : State()
    }

    private val disposables = CompositeDisposable()
    private val retrySubject = BehaviorSubject.createDefault(Unit)

    val showLoading = MutableLiveData(false)
    val showContent = MutableLiveData(false)
    val errorAllowRetry = MutableLiveData(false)
    val errorMessage = MutableLiveData<String>(null)
    val errorShow: LiveData<Boolean> = errorMessage.map { it != null }

    val content: LiveData<T> =
        retrySubject
            .switchMapSingle { loadContent() }
            .toLiveData(disposables)

    fun onRetryClicked() = retrySubject.onNext(Unit)

    abstract fun loadContent(): Single<T>

    fun renderState(state: State) {
        Timber.v("renderState() ${state.asLogString()}")
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
            State.ShowContent -> {
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

    private fun State.asLogString(): String {
        return when (this) {
            State.Initializing -> "State.Initializing"
            State.Loading -> "State.Loading"
            State.ShowContent -> "State.ShowContent"
            is State.Error -> "State.Error (message:='${getString(messageRes)}')"
        }
    }
}
