package se.allco.githubbrowser.app.user

import android.content.SharedPreferences
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Github token storage.
 * Uses SharedPreferences under hood.
 * All the methods return reactive streams which is not necessary for the current implementation since all the actions
 * are pretty quick and can be be invoked synchronously. But I think it is better to have it like this since it makes
 * the app ready for using asynchronous storage (other then SharedPreferences) under hood here later in future.
 */

@Singleton
class TokenCache @Inject constructor(
    private val prefs: SharedPreferences,
    private val tokenEncryptor: TokenEncryptor,
) {

    companion object {
        private val KEY = TokenCache::class.java.name + ".User"
    }

    fun read(): Maybe<GithubToken> =
        Maybe.create { emitter ->
            tryToReadToken()
                ?.let(emitter::onSuccess)
                ?: emitter.onComplete()
        }

    fun write(token: GithubToken): Completable = Completable.fromAction { saveToken(token) }
    fun erase(): Completable = Completable.fromAction { eraseToken() }

    private fun tryToReadToken(): GithubToken? =
        prefs.getString(KEY, null)
            .takeUnless { it.isNullOrBlank() }
            ?.let(tokenEncryptor::decrypt)

    private fun eraseToken() =
        prefs.edit().apply { remove(KEY) }.apply()

    private fun saveToken(token: GithubToken) =
        prefs.edit().apply { putString(KEY, tokenEncryptor.encrypt(token)) }.apply()
}
