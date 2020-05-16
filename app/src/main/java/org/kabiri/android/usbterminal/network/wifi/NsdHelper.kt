package org.kabiri.android.usbterminal.network.wifi

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import org.kabiri.android.usbterminal.Constants.Companion.SERVICE_NAME_PREFIX
import org.kabiri.android.usbterminal.Constants.Companion.SERVICE_TYPE
import java.util.*

/**
 * Created by Ali Kabiri on 12.05.20.
 */

class NsdHelper {

    companion object {
        private const val TAG = "NsdHelper"
    }

    private lateinit var nsdManager: NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

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

            // TODO - change this logic to store the UUID somewhere for this device
            val uuid = UUID.randomUUID().toString()

            serviceName = SERVICE_NAME_PREFIX + "_$uuid"
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        registrationListener = RegistrationListener()

        try {
            nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
                registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Something went wrong " +
                    "while trying to register the service ${e.message}")
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
        } ?: run { Log.e(TAG, "registration listener was null," +
                " probably it was unregistered before.") }
        discoveryListener?.let {
            nsdManager.stopServiceDiscovery(discoveryListener)
            discoveryListener = null
        } ?: run { Log.e(TAG, "discovery listener was null," +
                " probably it was unregistered before.") }
    }

    fun discoverService() {
        if (!::nsdManager.isInitialized) {
            Log.e(TAG, "NsdManager not initialized, " +
                    "probably the service was not registered or was already unregistered." +
                    " - Discovery aborted.")
            return // return if nsdManager is not initialized.
        }

        // prepare the discovery listener.
        val resolveListener = ResolveListener()
        discoveryListener = DiscoveryListener(nsdManager, resolveListener)

        // register the discovery callBack and discover services.
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }
}