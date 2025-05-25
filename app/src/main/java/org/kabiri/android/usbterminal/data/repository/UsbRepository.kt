package org.kabiri.android.usbterminal.data.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.Constants
import javax.inject.Inject

internal interface IUsbRepository {
    val usbDevice: SharedFlow<UsbDevice?>
    val infoMessageFlow: Flow<String>
    fun scanForArduinoDevices(): List<UsbDevice>
    fun requestUsbPermission(device: UsbDevice)
    fun onPermissionResult(device: UsbDevice?, granted: Boolean)
    fun disconnect()
}

internal class UsbRepository
@Inject constructor(
    private val context: Context,
    private val scope: CoroutineScope,
): IUsbRepository {
    private val usbManager = context.getSystemService(UsbManager::class.java)

    private val _usbDevice = MutableSharedFlow<UsbDevice?>(replay = 1)
    override val usbDevice: SharedFlow<UsbDevice?> = _usbDevice.asSharedFlow()

    private val _infoMessageFlow = MutableStateFlow<String>("")
    override val infoMessageFlow: SharedFlow<String> = _infoMessageFlow.asSharedFlow()

    override fun scanForArduinoDevices(): List<UsbDevice> {
        val deviceList = usbManager.deviceList
        return deviceList.values.toList()
    }

    override fun requestUsbPermission(device: UsbDevice) {
        val intent = Intent(Constants.ACTION_USB_PERMISSION)
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scope.launch { _usbDevice.emit(device) }
        ContextCompat.registerReceiver(
            context,
            UsbPermissionReceiver(this),
            IntentFilter(Constants.ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_EXPORTED
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    override fun onPermissionResult(device: UsbDevice?, granted: Boolean) {
        scope.launch {
            _infoMessageFlow.emit("Permission result: ${device?.vendorId}, granted: $granted\n")
            if (granted && device != null) {
                _usbDevice.emit(device)
            } else {
                _usbDevice.emit(null)
            }
        }
    }

    override fun disconnect() {
        scope.launch {
            _usbDevice.emit(null)
        }
    }
}