package org.kabiri.android.usbterminal.network.wifi

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private lateinit var nsdManager: NsdManager
    private lateinit var serverSocket: ServerSocket
    private val serviceNameHelper by inject<ServiceNameHelper>()
    private val wifiDeviceRepository by inject<WifiDeviceRepository>()
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: ResolveListener? = null

    private var mLocalPort: Int? = null
    private val _discoveredServices = MutableLiveData<ArrayList<NsdServiceInfo>>()
    val discoveredServices: LiveData<ArrayList<NsdServiceInfo>>
        get() = _discoveredServices

    fun registerService(context: Context) {

        if (::nsdManager.isInitialized) {
            Log.e(TAG, "NsdManager already initialized, " +
                    "probably the service was not unregistered properly.")
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
            Log.d(TAG, "registration listener cleared.")
        } ?: run { Log.e(TAG, "registration listener was null," +
                " probably it was unregistered before.") }
        discoveryListener?.let {
            nsdManager.stopServiceDiscovery(discoveryListener)
            discoveryListener = null
            Log.d(TAG, "discovery listener cleared.")
        } ?: run { Log.e(TAG, "discovery listener was null," +
                " probably it was unregistered before.") }
    }

    fun initializeServerSocket() {
        // Initialize a server socket on the next available port.
        serverSocket = ServerSocket(0).also { socket ->
            // Store the port to use it with service discovery.
            mLocalPort = socket.localPort
        }
    }

    fun discoverService() {
        if (!::nsdManager.isInitialized) {
            Log.e(TAG, "NsdManager not initialized, " +
                    "probably the service was not registered or was already unregistered." +
                    " - Discovery aborted.")
            return // return if nsdManager is not initialized.
        }

        // prepare the discovery listener.
        resolveListener = ResolveListener()
        discoveryListener = DiscoveryListener(nsdManager, _discoveredServices)

        // register the discovery callBack and discover services.
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun resolveService(service : NsdServiceInfo) {
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