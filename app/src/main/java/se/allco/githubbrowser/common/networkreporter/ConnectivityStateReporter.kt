package se.allco.githubbrowser.common.networkreporter

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface ConnectivityStateReporter {
    /**
     * Network connectivity reporter.
     * @return a stream which emits `true` if the device is/gets online or `false` otherwise,
     */
    fun states(): Observable<Boolean>
}

class NetworkConnectivityReporterImpl @Inject constructor(
    private val networkReporterFactory: NetworkReporterApi.Factory
) : ConnectivityStateReporter {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 200L
    }

    private val connectivityStatesStream: Observable<Boolean> by lazy {
        networkReporterFactory
            .create()
            .connectivityStatesStream
            .startWithItem(false)
            .distinctUntilChanged()
            .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS, Schedulers.io())
            .replay(1)
            .refCount()
    }

    override fun states(): Observable<Boolean> = connectivityStatesStream
}
