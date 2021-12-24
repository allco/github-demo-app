package se.allco.githubbrowser.app.main.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.utils.combine
import se.allco.githubbrowser.utils.getString
import se.allco.githubbrowser.utils.map
import se.allco.githubbrowser.utils.plusAssign
import se.allco.githubbrowser.utils.toLiveData
import se.allco.githubbrowser.utils.ui.attachSmartLoading

class AccountViewModel @Inject constructor(
    application: Application,
    private val repository: AccountRepository,
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()
    private val _showLoading = MutableLiveData(false)

    private val _data =
        repository
            .getAccount()
            .attachSmartLoading { showLoadingLiveData = _showLoading }
            .doOnError { errorMessage.postValue(getString(R.string.error_generic)) }
            .toLiveData()

    val userName: LiveData<String> = _data.map { it?.name ?: "" }
    val imageUrl = _data.map { it?.imageUrl ?: "" }

    val errorMessage = MutableLiveData<String>(null)
    val showError: LiveData<Boolean> = errorMessage.map { !it.isNullOrBlank() }

    val showLoading: LiveData<Boolean> =
        _showLoading
            .combine(showError, false) { loading, error -> loading == true && error != true }

    val showContent: LiveData<Boolean> =
        userName
            .combine(showLoading, false) { content, loading -> content != null && loading != true }
            .combine(showError, false) { content, error -> content == true && error != true }

    fun onLogout() {
        disposables +=
            repository
                .logoutUser()
                .attachSmartLoading { showLoadingLiveData = _showLoading }
                .doOnError { errorMessage.postValue(getString(R.string.error_generic)) }
                .subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
