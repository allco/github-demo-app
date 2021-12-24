package se.allco.githubbrowser.app.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.allco.githubbrowser.app.login._di.LoginScope
import se.allco.githubbrowser.common.ui.toSingleLiveEvent
import javax.inject.Inject


@LoginScope
class LoginMediator @Inject constructor() {

    private val _navigateToManualLogin = MutableLiveData<Unit>()
    val navigateToManualLogin: LiveData<Unit> = _navigateToManualLogin.toSingleLiveEvent()

    private val _finishFlow = MutableLiveData<Unit>()
    val finishFlow: LiveData<Unit> = _finishFlow.toSingleLiveEvent()

    fun navigateToManualLogin() {
        _navigateToManualLogin.postValue(Unit)
    }

    fun finishFlow() {
        _finishFlow.postValue(Unit)
    }
}
