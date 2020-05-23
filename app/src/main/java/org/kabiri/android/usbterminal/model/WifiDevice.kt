package org.kabiri.android.usbterminal.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifiDevices")
data class WifiDevice (
    @PrimaryKey @ColumnInfo(name = "serviceId") val serviceId: String,
    val name: String
)