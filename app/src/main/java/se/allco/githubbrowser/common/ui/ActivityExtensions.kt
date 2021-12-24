package se.allco.githubbrowser.common.ui

import androidx.fragment.app.FragmentActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import se.allco.githubbrowser.app.login.LoginActivity
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserComponentHolder
import se.allco.githubbrowser.utils.attachLifecycleEventsObserver

fun FragmentActivity.ensureUserLoggedIn(onValidUser: () -> Unit) {

    fun currentUser(): User =
        UserComponentHolder
            .getUserComponent(this)
            .getCurrentUser()

    fun currentUserFeed(): Observable<User> =
        UserComponentHolder
            .getInstance(this)
            .getUserComponentsFeed()
            .map { it.getCurrentUser() }

    fun onUserChanged(user: User, callback: (() -> Unit)? = null) {
        when (user) {
            is User.Valid -> callback?.invoke()
            else -> {
                startActivity(LoginActivity.createIntent(this))
                finishAfterTransition()
            }
        }
    }

    val disposables = SerialDisposable()
    lifecycle.attachLifecycleEventsObserver {
        onResumed = {
            disposables.set(
                currentUserFeed()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(::onUserChanged)
            )
        }
        onPaused = {
            disposables.set(Disposable.empty())
        }
    }

    onUserChanged(currentUser(), onValidUser)
}
