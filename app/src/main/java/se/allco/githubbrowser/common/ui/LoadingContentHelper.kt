package se.allco.githubbrowser.common.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.subjects.BehaviorSubject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.common.utils.map
import javax.inject.Inject

class LoadableContentViewModel @Inject constructor(private val context: Context) {

    val retrySubject = BehaviorSubject.createDefault(Unit)

    val showLoading = MutableLiveData(false)
    val showContent = MutableLiveData(false)
    val errorAllowRetry = MutableLiveData(false)
    val errorMessage = MutableLiveData<String>(null)
    val errorShow: LiveData<Boolean> = errorMessage.map { it != null }

    fun onRetryClicked() = retrySubject.onNext(Unit)

    fun renderGenericError(allowRetry: Boolean) {
        renderError(context.getString(R.string.error_generic), allowRetry)
    }

    fun renderError(message: String, allowRetry: Boolean) {
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

    fun renderLoading() {
        showContent.postValue(false)
        errorMessage.postValue(null)
        showLoading.postValue(true)
        errorAllowRetry.postValue(false)
    }

    fun renderContentReady() {
        showLoading.postValue(false)
        errorMessage.postValue(null)
        showContent.postValue(true)
        errorAllowRetry.postValue(false)
    }
}
