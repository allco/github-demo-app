package se.allco.githubbrowser.app.login.autologin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.utils.toLiveData
import se.allco.githubbrowser.utils.ui.attachSmartLoading

class AutoLoginViewModel @Inject constructor(
    model: AutoLoginModel,
    application: Application,
) : AndroidViewModel(application) {

    val showLoading = MutableLiveData(false)

    val result: LiveData<User> =
        model
            .login()
            .attachSmartLoading { showLoadingLiveData = showLoading }
            .toLiveData()
}
