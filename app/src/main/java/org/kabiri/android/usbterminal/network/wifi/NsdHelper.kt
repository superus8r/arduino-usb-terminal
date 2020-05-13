package org.kabiri.android.usbterminal.network.wifi

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import java.net.ServerSocket

/**
 * Created by Ali Kabiri on 12.05.20.
 */

private const val TAG = "NsdHelper"

class NsdHelper {
    lateinit var nsdManager: NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null

    fun registerService(context: Context, port: Int) {

        if (::nsdManager.isInitialized) {
            Log.e(TAG, "NsdManager already initialized, " +
                    "probably the service was not unregistered properly.")
            return
        }

        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = "NsdArduino"
            serviceType = "_nsdarduino._tcp"
            setPort(port)
        }

        registrationListener = getListener()

        try {
            nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
                registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }

    fun unregisterService() {
        if (!::nsdManager.isInitialized) {
            Log.e(TAG, "NsdManager not initialized, " +
                    "probably the service was not registered or was already unregistered.")
            return
        }
        registrationListener?.let {
            nsdManager.unregisterService(registrationListener)
            registrationListener = null
        } ?: run { Log.e(TAG, "listener was null, probably it was unregistered before.") }
    }

    private fun getListener(): NsdManager.RegistrationListener = object : NsdManager.RegistrationListener {

        var mServiceName = ""

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
}

var mLocalPort = 0
var serverSocket: ServerSocket? = null
fun initializeServerSocket() {
    // Initialize a server socket on the next available port.
    serverSocket = ServerSocket(0).also { socket ->
        // Store the chosen port.
        mLocalPort = socket.localPort
        Log.d(TAG, "socket port initialized: ${socket.localPort}")
    }
}