package se.allco.githubbrowser.app.login.manuallogin

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import se.allco.githubbrowser.app.user.User
import javax.inject.Inject

typealias GithubCode = String

class ManualLoginModel @Inject constructor(
    private val repository: ManualLoginRepository,
) {

    fun authenticateWithCode(code: String): Single<User.Valid> =
        repository
            .fetchAccessToken(code)
            .flatMap(repository::fetchUserData)
            .flatMap(::cacheUserData)

    private fun cacheUserData(user: User.Valid): Single<User.Valid> =
        repository
            .writeCachedToken(user.token)
            .andThen(Single.just(user))

    fun createErrorHandler(): Completable = repository.clearUserData()
}
