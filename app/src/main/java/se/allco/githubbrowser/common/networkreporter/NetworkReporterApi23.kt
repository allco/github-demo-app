package se.allco.githubbrowser.common.networkreporter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import io.reactivex.rxjava3.core.Observable

// TODO(alsk): make it injectable
internal class NetworkReporterApi23(context: Context) {
    companion object {
        @Suppress("DEPRECATION")
        private val INTENT_FILTER = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }

    val connectivityStatesStream: Observable<Boolean> = Observable.create { emitter ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when {
                    intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY) -> emitter.onNext(
                        false
                    )
                    else -> {
                        tryToExtractConnectedStatus(intent)?.let(emitter::onNext)
                    }
                }
            }
        }

        context.registerReceiver(receiver, INTENT_FILTER)
        emitter.setCancellable { context.unregisterReceiver(receiver) }
    }

    @Suppress("DEPRECATION")
    private fun tryToExtractConnectedStatus(intent: Intent): Boolean? =
        (intent.extras?.get(ConnectivityManager.EXTRA_NETWORK_INFO) as? android.net.NetworkInfo)
            ?.isConnected
}
