package org.kabiri.android.usbterminal.network.wifi

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

/**
 * Created by Ali Kabiri on 13.05.20.
 */
class RegistrationListener: NsdManager.RegistrationListener {

    companion object {
        const val TAG = "RegistrationListener"
    }

    private var mServiceName = ""

    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
        // Save the service name. Android may have changed it in order to
        // resolve a conflict, so update the name you initially requested
        // with the name Android actually used.
        mServiceName = serviceInfo.serviceName
        Log.d(TAG, "service registered: $mServiceName")
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        // Registration failed! Put debugging code here to determine why.
        Log.d(TAG, "registration failed:${serviceInfo.serviceName}. code:$errorCode")
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
        // Service has been unregistered. This only happens when you call
        // NsdManager.unregisterService() and pass in this listener.
        Log.d(TAG, "service un-registered: ${serviceInfo.serviceName}")
    }

    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        // Un-registration failed. Put debugging code here to determine why.
        Log.d(TAG, "un-registration failed: ${serviceInfo.serviceName}, code:$errorCode")
    }
}