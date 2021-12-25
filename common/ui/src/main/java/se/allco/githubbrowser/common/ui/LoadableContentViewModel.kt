package se.allco.githubbrowser.common.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import se.allco.githubbrowser.common.utils.map

class LoadableContentViewModel @Inject constructor(private val context: Context) {

    private val retrySubject = BehaviorSubject.createDefault(Unit)

    val showLoading = MutableLiveData(false)
    val showContent = MutableLiveData(false)
    val errorAllowRetry = MutableLiveData(false)
    val errorMessage = MutableLiveData<String>(null)
    val errorShow: LiveData<Boolean> = errorMessage.map { it != null }

    fun onRetryClicked() = retrySubject.onNext(Unit)

    fun <T : Any> runRetryable(block: () -> Single<T>): Observable<T> =
        retrySubject.switchMapSingle { block() }

    fun renderStateError(@StringRes messageRes: Int, allowRetry: Boolean) {
        renderStateError(context.getString(messageRes), allowRetry)
    }

    fun renderStateError(message: String, allowRetry: Boolean) {
        showContent.postValue(false)
        showLoading.postValue(false)
        errorMessage.postValue(message)
        errorAllowRetry.postValue(allowRetry)
    }

    fun renderInitialisation() {
        showContent.postValue(false)
        showLoading.postValue(false)
        errorMessage.postValue(null)
        errorAllowRetry.postValue(false)
    }

    fun renderStateLoading() {
        showContent.postValue(false)
        errorMessage.postValue(null)
        showLoading.postValue(true)
        errorAllowRetry.postValue(false)
    }

    fun renderStateContentReady() {
        showLoading.postValue(false)
        errorMessage.postValue(null)
        showContent.postValue(true)
        errorAllowRetry.postValue(false)
    }
}
