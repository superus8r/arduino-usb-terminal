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
import org.kabiri.android.usbterminal.R
import javax.inject.Inject

internal interface IUsbRepository {
    val usbDevice: SharedFlow<UsbDevice?>
    val infoMessageFlow: Flow<String>
    fun scanForArduinoDevices(): List<UsbDevice>
    fun requestUsbPermission(device: UsbDevice)
    fun hasPermission(device: UsbDevice): Boolean
    fun onPermissionResult(device: UsbDevice?, granted: Boolean)
    fun onDeviceAttached(device: UsbDevice?)
    fun onDeviceDetached(device: UsbDevice?)
    fun onUnknownAction(intent: Intent?)
    fun disconnect()
}

internal class UsbRepository
@Inject constructor(
    private val context: Context,
    private val scope: CoroutineScope,
): IUsbRepository {
//    private val usbManager = context.getSystemService(UsbManager::class.java)

    private val _usbDevice = MutableSharedFlow<UsbDevice?>(replay = 1)
    override val usbDevice: SharedFlow<UsbDevice?> = _usbDevice.asSharedFlow()

    private val _infoMessageFlow = MutableStateFlow<String>("")
    override val infoMessageFlow: SharedFlow<String> = _infoMessageFlow.asSharedFlow()

    override fun scanForArduinoDevices(): List<UsbDevice> {
        val usbManager = context.getSystemService(UsbManager::class.java)
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
        ContextCompat.registerReceiver(
            context,
            UsbPermissionReceiver(this@UsbRepository),
            IntentFilter(Constants.ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_EXPORTED
        )
        scope.launch {
            _usbDevice.emit(device)
            val usbManager = context.getSystemService(UsbManager::class.java)
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    override fun hasPermission(device: UsbDevice): Boolean {
        val usbManager = context.getSystemService(UsbManager::class.java)
        return usbManager.hasPermission(device)
    }

    override fun onPermissionResult(
        device: UsbDevice?,
        granted: Boolean
    ) {
        val deviceInfo =
            "${device?.manufacturerName} " +
                    "${device?.productName} " +
                    "${device?.vendorId} " +
                    "${device?.productId}"
        scope.launch {
            if (granted) {
                val infoMsg =
                    "${context.getString(R.string.breceiver_info_usb_permission_granted)} " +
                            deviceInfo
                _infoMessageFlow.emit("\n$infoMsg")
            } else {
                val errorMsg =
                    "${context.getString(R.string.breceiver_error_usb_permission_denied)} " +
                        deviceInfo
                _infoMessageFlow.emit(errorMsg)
            }
        }
    }

    override fun onDeviceAttached(device: UsbDevice?) {
        scope.launch {
            val attachedMsg = context.getString(R.string.breceiver_info_device_attached)
            _infoMessageFlow.emit(attachedMsg)
        }
    }

    override fun onDeviceDetached(device: UsbDevice?) {
        scope.launch {
            val detachedMsg = context.getString(R.string.breceiver_info_device_detached)
            _infoMessageFlow.emit(detachedMsg)
        }
    }

    override fun onUnknownAction(intent: Intent?) {
        scope.launch {
            val unknownActionMsg = context.getString(R.string.breceiver_info_unknown_intent_action)
            _infoMessageFlow.emit(unknownActionMsg)
        }
    }

    override fun disconnect() {
        scope.launch {
            _usbDevice.emit(null)
        }
    }
}