package org.kabiri.android.usbterminal.data.repository

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.app.PendingIntent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kabiri.android.usbterminal.Constants
import org.kabiri.android.usbterminal.extensions.isCloneArduinoBoard
import org.kabiri.android.usbterminal.extensions.isOfficialArduinoBoard
import org.kabiri.android.usbterminal.model.ArduinoDevice
import javax.inject.Inject

internal interface IUsbRepository {
    val usbDevice: StateFlow<UsbDevice?>
    fun scanForArduinoDevices(onResult: (device: ArduinoDevice?) -> Unit)
    fun requestUsbPermission(device: UsbDevice)
    fun onPermissionResult(device: UsbDevice?, granted: Boolean)
    fun disconnect()
}

internal class UsbRepository
@Inject constructor(
    private val context: Context
): IUsbRepository {
    private val usbManager = context.getSystemService(UsbManager::class.java)

    private val _usbDevice = MutableStateFlow<UsbDevice?>(null)
    override val usbDevice: StateFlow<UsbDevice?> get() = _usbDevice

    override fun scanForArduinoDevices(onResult: (device: ArduinoDevice?) -> Unit) {
        val deviceList = usbManager.deviceList
        val device = deviceList.values.firstOrNull { usbDevice ->
            usbDevice.isOfficialArduinoBoard() || usbDevice.isCloneArduinoBoard()
        }
        if (device != null) {
            return onResult(ArduinoDevice(device))
        }
        return onResult(null)
    }

    override fun requestUsbPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(Constants.ACTION_USB_PERMISSION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        _usbDevice.value = device
        ContextCompat.registerReceiver(
            context,
            UsbPermissionReceiver(this),
            IntentFilter(Constants.ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_EXPORTED
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    override fun onPermissionResult(device: UsbDevice?, granted: Boolean) {
        if (granted && device != null) {
            _usbDevice.value = device
        } else {
            _usbDevice.value = null
        }
    }

    override fun disconnect() {
        _usbDevice.value = null
    }
}