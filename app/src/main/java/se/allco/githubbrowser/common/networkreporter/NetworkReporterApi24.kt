package se.allco.githubbrowser.common.networkreporter

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
class NetworkReporterApi24 @Inject constructor(context: Context) : NetworkReporterApi {
    override val connectivityStatesStream: Observable<Boolean> = Observable.create { emitter ->
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = emitter.onNext(true)
            override fun onLost(network: Network) = emitter.onNext(false)
        }

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(callback)
        emitter.setCancellable { cm.unregisterNetworkCallback(callback) }
    }
}
