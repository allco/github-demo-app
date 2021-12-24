package se.allco.githubbrowser.common.networkreporter

import android.content.Context
import android.os.Build
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

class NetworkConnectivityReporterImpl @Inject constructor(context: Context) :
    ConnectivityStateReporter {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 200L
    }

    @Suppress("MagicNumber")
    private val connectivityStatesStream: Observable<Boolean> by lazy {
        when {
            Build.VERSION.SDK_INT >= 24 -> NetworkReporterApi24(context).connectivityStatesStream
            else -> NetworkReporterApi23(context).connectivityStatesStream
        }
            .startWithItem(false)
            .distinctUntilChanged()
            .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS, Schedulers.io())
            .replay(1)
            .refCount()
    }

    override fun states(): Observable<Boolean> = connectivityStatesStream
}
