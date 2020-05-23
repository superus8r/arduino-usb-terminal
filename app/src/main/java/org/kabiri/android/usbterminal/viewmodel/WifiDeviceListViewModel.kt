package org.kabiri.android.usbterminal.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.kabiri.android.usbterminal.SettingsActivity
import org.kabiri.android.usbterminal.data.SettingsReader
import org.kabiri.android.usbterminal.data.WifiDeviceRepository
import org.kabiri.android.usbterminal.model.WifiDevice
import org.kabiri.android.usbterminal.network.wifi.NsdHelper
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 *  Created by Ali Kabiri on 23.05.20.
 * The ViewModel for [WifiDeviceListFragment].
 */
class WifiDeviceListViewModel internal constructor(
    wifiDeviceRepository: WifiDeviceRepository
): ViewModel(), KoinComponent {

    private val settings by inject<SettingsReader>()

    val wifiDevices: LiveData<List<WifiDevice>> = wifiDeviceRepository.getWifiDevices()

    fun discoverWifiDevices(context: Context) {
        // TODO - This will be removed from here: testing the settings reader
        val helper = NsdHelper()

        settings.discoveryEnabledListener = {
            Log.d(SettingsActivity.TAG, "enabled:$it")

            // TODO - change the hardcoded font
            if (it) {
                helper.registerService(context, 999)
                helper.discoverService()
            } // register the service
            else helper.unregisterService() // unregister the service.
        }
    }
}