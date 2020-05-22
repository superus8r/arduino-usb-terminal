package org.kabiri.android.usbterminal.network.wifi

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import org.kabiri.android.usbterminal.Constants
import org.kabiri.android.usbterminal.data.ServiceNameHelper
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Ali Kabiri on 13.05.20.
 */
class DiscoveryListener(
    private val nsdManager: NsdManager,
    private val resolveListener: NsdManager.ResolveListener): NsdManager.DiscoveryListener, KoinComponent {

    companion object {
        const val TAG = "DiscoveryListener"
    }

    private val serviceNameHelper by inject<ServiceNameHelper>()

    // Called as soon as service discovery begins.
    override fun onDiscoveryStarted(regType: String) {
        Log.d(TAG, "Service discovery started")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        // A service was found! Do something with it.
        Log.d(TAG, "Service discovery success $service")
        when {
            service.serviceType != Constants.SERVICE_TYPE -> // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
            service.serviceName == serviceNameHelper.serviceName -> // The name of the service tells the user what they'd be
                // connecting to.
                Log.d(TAG, "Same machine: ${service.serviceName}")
            service.serviceName.contains(serviceNameHelper.serviceName) -> {// determine the connection info
                // for that discovered service
                nsdManager.resolveService(service, resolveListener)
                Log.d(TAG, "resolved service: ${service.host}")
            }
        }
    }

    override fun onServiceLost(service: NsdServiceInfo) {
        // When the network service is no longer available.
        // Internal bookkeeping code goes here.
        Log.e(TAG, "service lost: $service")
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Log.i(TAG, "Discovery stopped: $serviceType")
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.e(TAG, "Discovery failed: Error code:$errorCode")
        nsdManager.stopServiceDiscovery(this)
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.e(TAG, "Discovery failed: Error code:$errorCode")
        nsdManager.stopServiceDiscovery(this)
    }
}