package se.allco.githubbrowser.app.login

import se.allco.githubbrowser.app.login._di.LoginScope
import se.allco.githubbrowser.app.login.autologin.AutoLoginFragment
import se.allco.githubbrowser.app.login.manuallogin.ManualLoginFragment
import se.allco.githubbrowser.app.user.User
import timber.log.Timber
import javax.inject.Inject

@LoginScope
class LoginUseCase @Inject constructor(
    private val mediator: LoginMediator,
    private val repository: LoginRepository,
) : AutoLoginFragment.Listener,
    ManualLoginFragment.Listener {

    override fun onAutoLoginResult(user: User) =
        when (user) {
            is User.Valid -> onUserLoggedIn(user)
            else -> mediator.navigateToManualLogin()
        }

    override fun onManualLoginResult(user: User.Valid) = onUserLoggedIn(user)

    private fun onUserLoggedIn(user: User.Valid) {
        Timber.v("onUserLoggedIn(), $user")
        repository.switchLoggedInUser(user)
        mediator.finishFlow()
    }
}
