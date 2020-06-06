package org.kabiri.android.usbterminal.network.wifi

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import org.kabiri.android.usbterminal.SERVICE_TYPE
import org.kabiri.android.usbterminal.data.ServiceNameHelper
import org.kabiri.android.usbterminal.data.WifiDeviceRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Ali Kabiri on 13.05.20.
 *
 * Whenever a service is found on the network, onServiceFound() method will be called.
 */
class DiscoveryListener(
    private val nsdManager: NsdManager,
    private val discoveredDeviceListener: (NsdServiceInfo) -> Unit
) : NsdManager.DiscoveryListener, KoinComponent {

    companion object {
        const val TAG = "DiscoveryListener"
    }

    private val serviceNameHelper by inject<ServiceNameHelper>()
    private val wifiDeviceRepository by inject<WifiDeviceRepository>()

    // Called as soon as service discovery begins.
    override fun onDiscoveryStarted(regType: String) {
        Log.d(TAG, "Service discovery started")
        // remove the old results from the db.
        wifiDeviceRepository.deleteAll()
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        // A service was found! Do something with it.
        Log.d(TAG, "Service discovered: $service")
        when {
            service.serviceType != SERVICE_TYPE -> {
                // Service type is the string containing
                // the protocol and transport layer for this service.
                Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
            }
            service.serviceName == serviceNameHelper.serviceName -> {
                // The name of the service tells the user what they'd be connecting to.
                // same device in this case.
                Log.d(TAG, "Same machine: ${service.serviceName}")
            }
            service.serviceType == SERVICE_TYPE  -> {
                // service type is similar, but names are different.
                // it means another device is running the app and has discovery on.

                // determine the connection info for that discovered service
                discoveredDeviceListener(service)
                Log.d(TAG, "Service discovered, same type, another device: $service")
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