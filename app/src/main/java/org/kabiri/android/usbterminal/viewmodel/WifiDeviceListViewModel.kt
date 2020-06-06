package org.kabiri.android.usbterminal.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.SettingsActivity
import org.kabiri.android.usbterminal.data.SettingsReader
import org.kabiri.android.usbterminal.data.WifiDeviceRepository
import org.kabiri.android.usbterminal.model.WifiDevice
import org.kabiri.android.usbterminal.network.wifi.NsdHelper
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 *  Created by Ali Kabiri on 23.05.20.
 * The ViewModel for WifiDeviceListFragment
 */
class WifiDeviceListViewModel internal constructor(
    private val wifiDeviceRepository: WifiDeviceRepository
): ViewModel(), KoinComponent {

    private val mNsdHelper by inject<NsdHelper>()
    private val settings by inject<SettingsReader>()
    val wifiDevices: LiveData<List<WifiDevice>> = wifiDeviceRepository.getWifiDevices()

    private val _showRemoteServers = MutableLiveData<Boolean>().apply { postValue(false) }
    val showRemoteServers: LiveData<Boolean> // avoid having public mutable live data.
        get() = _showRemoteServers

    /**
     * checks which device mode is selected by the user in the settings and
     * registers and discovers the service if necessary.
     */
    fun handleDeviceMode(context: Context) {

        settings.deviceModeListener = {
            Log.d(SettingsActivity.TAG, "device mode selected: $it")
            when (it) {
                context.getString(R.string.settings_value_device_mode_server) -> {
                    _showRemoteServers.postValue(false)
                    mNsdHelper.unregisterService(context)
                    mNsdHelper.registerService(context)
                }
                context.getString(R.string.settings_value_device_mode_client) -> {
                    _showRemoteServers.postValue(true)
                    mNsdHelper.unregisterService(context)
                    mNsdHelper.discoverService(context)
                }
                else -> {
                    _showRemoteServers.postValue(false)
                    mNsdHelper.unregisterService(context)
                }
            }
        }
    }

    fun observeDevices() {
        mNsdHelper.discoveredDeviceListener = {
            val device = WifiDevice(
                it.serviceName,
                it.serviceName.substringBefore('-'))
            wifiDeviceRepository.insert(device)
        }
    }
}