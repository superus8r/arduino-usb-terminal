package org.kabiri.android.usbterminal.network.wifi

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import org.kabiri.android.usbterminal.SERVICE_TYPE
import org.kabiri.android.usbterminal.data.ServiceNameHelper
import org.kabiri.android.usbterminal.data.WifiDeviceRepository
import org.kabiri.android.usbterminal.model.WifiDevice
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.net.ServerSocket

/**
 * Created by Ali Kabiri on 12.05.20.
 */

class NsdHelper: KoinComponent {

    companion object {
        private const val TAG = "NsdHelper"
    }

    private var nsdManager: NsdManager? = null
    private lateinit var serverSocket: ServerSocket
    private val serviceNameHelper by inject<ServiceNameHelper>()
    private val wifiDeviceRepository by inject<WifiDeviceRepository>()
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: ResolveListener? = null

    private var mLocalPort: Int? = null
    var discoveredDeviceListener = { _: NsdServiceInfo -> Unit }

    fun registerService(context: Context) {

        initializeNsdManager(context)
        val nsdManager = nsdManager ?: run {
            Log.e(TAG, "Register service failed: NsdManager was not initialized.")
            return
        }

        initializeServerSocket() // inits serverSocked and mLocalPort (next available port)
        mLocalPort = mLocalPort?.let { mLocalPort } ?: run {
            // local port was null, break the operation.
            Log.e(TAG, "Local port was null, probably no ports are available!")
            return
        }

        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = serviceNameHelper.serviceName
            serviceType = SERVICE_TYPE
            port = mLocalPort as Int
        }

        registrationListener = RegistrationListener()

        // register the service on the network.
        try {
            nsdManager.apply {
                registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Something went wrong " +
                    "while trying to register the service ${e.message}")
        }
    }

    /**
     * Unregisters the registration listener and or the discovery listener is they exist.
     */
    fun unregisterService(context: Context) {
        if (nsdManager == null) {
            Log.e(TAG, "Unregistering: NsdManager not initialized, " +
                    "probably the service was not registered or was already unregistered.")
        } else {
            Log.d(TAG, "Unregistering: initializing NsdManager to force unregistering.")
            initializeNsdManager(context)
        }

        val nsdManager = nsdManager ?: run {
            Log.e(TAG, "Unregistering: failed: NsdManager was not/could not be initialized.")
            return
        }

        registrationListener?.let {
            nsdManager.unregisterService(registrationListener)
            registrationListener = null
            Log.d(TAG, "registration listener cleared.")
        } ?: run { Log.e(TAG, "registration listener was null," +
                " probably it was unregistered before.") }
        discoveryListener?.let {
            nsdManager.stopServiceDiscovery(discoveryListener)
            discoveryListener = null
            Log.d(TAG, "discovery listener cleared.")
        } ?: run { Log.e(TAG, "discovery listener was null," +
                " probably it was unregistered before.") }
        this.nsdManager = null
    }

    private fun initializeNsdManager(context: Context) {
        nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager)
    }

    private fun initializeServerSocket() {
        // Initialize a server socket on the next available port.
        serverSocket = ServerSocket(0).also { socket ->
            // Store the port to use it with service discovery.
            mLocalPort = socket.localPort
        }
    }

    fun discoverService(context: Context) {
        initializeNsdManager(context)
        val nsdManager = nsdManager ?: run {
            Log.e(TAG, "Discovery failed: Could not initialize NsdManager")
            return
        }

        // prepare the discovery listener.
        resolveListener = ResolveListener()
        discoveryListener = DiscoveryListener(nsdManager, discoveredDeviceListener)

        // register the discovery callBack and discover services.
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun resolveService(service : NsdServiceInfo) {
        val nsdManager = nsdManager ?: run {
            Log.e(TAG, "Resolve failed: NsdManager was not initialized.")
            return
        }
        nsdManager.resolveService(service, resolveListener)
        insertWifiDevice(service)
    }

    private fun insertWifiDevice(service: NsdServiceInfo) {
        val serviceName = service.serviceName ?: ""
        wifiDeviceRepository.insert(
            WifiDevice(
                // avoid calling toString on null object.
                serviceName = serviceName,
                simpleName = serviceName.substringBefore('-')
            )
        )
    }
}