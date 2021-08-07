package se.allco.githubbrowser.app.user

import com.google.gson.GsonBuilder
import javax.inject.Inject
import javax.inject.Provider

/**
 * Encrypts/Decrypts GithubToken.
 * Current implementation just turns GithubToken to plain JSON
 * which could be improved by applying some real encryption algorithm to resulted JSON.
 */

class TokenEncryptor @Inject constructor(
    private val gsonBuilderProvider: Provider<GsonBuilder>,
) {

    fun encrypt(token: GithubToken): String =
        gsonBuilderProvider.get().create().toJson(token)

    fun decrypt(encryptedToken: String): GithubToken =
        gsonBuilderProvider
            .get()
            .create()
            .fromJson(encryptedToken, GithubToken::class.java)
}
