package org.kabiri.android.usbterminal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.kabiri.android.usbterminal.data.WifiDeviceRepository
import org.kabiri.android.usbterminal.model.WifiDevice

/**
 *  Created by Ali Kabiri on 23.05.20.
 * The ViewModel for [WifiDeviceListFragment].
 */
class WifiDeviceListViewModel internal constructor(
    private val savedStateHandle: SavedStateHandle,
    wifiDeviceRepository: WifiDeviceRepository
): ViewModel() {
    val wifiDevices: LiveData<List<WifiDevice>> = wifiDeviceRepository.getWifiDevices()
}