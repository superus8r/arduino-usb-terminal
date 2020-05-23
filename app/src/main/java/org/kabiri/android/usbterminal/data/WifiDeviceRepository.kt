package org.kabiri.android.usbterminal.data

import org.kabiri.android.usbterminal.model.WifiDevice

/**
 *  Created by Ali Kabiri on 23.05.20.
 */
class WifiDeviceRepository private constructor(private val wifiDeviceDao: WifiDeviceDao) {

    fun getWifiDevices() = wifiDeviceDao.getWifiDevices()
    fun insert(wifiDevice: WifiDevice) = wifiDeviceDao.insert(wifiDevice)
    fun deleteAll() = wifiDeviceDao.deleteAll()

    companion object {

        // for singleton instantiation during tests
        @Volatile private var instance: WifiDeviceRepository? = null

        fun getInstance(wifiDeviceDao: WifiDeviceDao) =
            instance ?: synchronized(this) {
                // create a new instance if it is already not created.
                instance ?: WifiDeviceRepository(wifiDeviceDao).also { instance = it }
            }
    }
}