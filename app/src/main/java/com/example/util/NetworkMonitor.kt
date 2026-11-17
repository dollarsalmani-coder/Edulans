package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkStatus(
    val isConnected: Boolean = true,
    val isMetered: Boolean = false
)

class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _status = MutableStateFlow(readCurrentStatus())
    val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _status.value = readCurrentStatus()
        }

        override fun onLost(network: Network) {
            _status.value = readCurrentStatus()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            _status.value = readCurrentStatus()
        }
    }

    init {
        runCatching {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, callback)
        }
    }

    private fun readCurrentStatus(): NetworkStatus {
        val cm = connectivityManager ?: return NetworkStatus(isConnected = false, isMetered = false)
        val activeNetwork = cm.activeNetwork ?: return NetworkStatus(isConnected = false, isMetered = false)
        val caps = cm.getNetworkCapabilities(activeNetwork)
            ?: return NetworkStatus(isConnected = false, isMetered = false)

        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isUnmetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        return NetworkStatus(
            isConnected = hasInternet,
            isMetered = hasInternet && !isUnmetered
        )
    }

    fun release() {
        runCatching {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }
}
