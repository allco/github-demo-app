package se.allco.githubbrowser.common.networkreporter

import android.os.Build
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Provider

interface NetworkReporterApi {

    val connectivityStatesStream: Observable<Boolean>

    class Factory @Inject constructor(
        private val reporterApi24Provider: Provider<NetworkReporterApi24>,
        private val reporterApi23Provider: Provider<NetworkReporterApi23>,
    ) {
        @Suppress("MagicNumber")
        fun create(): NetworkReporterApi =
            when {
                Build.VERSION.SDK_INT >= 24 -> reporterApi24Provider.get()
                else -> reporterApi23Provider.get()
            }
    }
}
