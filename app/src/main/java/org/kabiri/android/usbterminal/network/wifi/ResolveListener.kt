package org.kabiri.android.usbterminal.network.wifi

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import org.kabiri.android.usbterminal.network.wifi.NsdHelper.Companion.SERVICE_NAME
import java.net.InetAddress

/**
 * Created by Ali Kabiri on 13.05.20.
 */
class ResolveListener: NsdManager.ResolveListener {

    companion object {
        const val TAG = "ResolveManager"
    }

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        // Called when the resolve fails. Use the error code to debug.
        Log.e(TAG, "Resolve failed: $errorCode")
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Log.e(TAG, "Resolve Succeeded. $serviceInfo")

        if (serviceInfo.serviceName == SERVICE_NAME) {
            Log.d(TAG, "Same IP.")
            return
        }
        val mService = serviceInfo
        serviceInfo.port
        val port: Int = serviceInfo.port
        val host: InetAddress = serviceInfo.host
    }
}