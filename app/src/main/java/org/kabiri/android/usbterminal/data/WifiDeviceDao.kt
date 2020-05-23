package org.kabiri.android.usbterminal.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.kabiri.android.usbterminal.model.WifiDevice

/**
 *
 * Created by Ali Kabiri on 23.05.20.
 */
@Dao
interface WifiDeviceDao {
    @Query("SELECT * FROM wifiDevices ORDER BY name")
    fun getWifiDevices(): LiveData<List<WifiDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wifiDevices: List<WifiDevice>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wifiDevice: WifiDevice)
}