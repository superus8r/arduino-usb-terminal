package org.kabiri.android.usbterminal.arduino

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbManager.EXTRA_DEVICE
import android.hardware.usb.UsbManager.EXTRA_PERMISSION_GRANTED
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kabiri.android.usbterminal.Constants
import org.kabiri.android.usbterminal.R
import kotlin.collections.plusAssign

/**
 * Created by Ali Kabiri on 13.04.20.
 *
 * Gets informed when the user has granted the app interaction with Arduino.
 */
class ArduinoPermissionBroadcastReceiver: BroadcastReceiver() {

    companion object {
        private const val TAG = "ArduinoPermReceiver"
    }

    private val _liveOutput = MutableStateFlow("")
    private val _liveInfoOutput = MutableStateFlow("")
    private val _liveErrorOutput = MutableStateFlow("")

    val liveOutput: StateFlow<String>
        get() = _liveOutput
    val liveInfoOutput: StateFlow<String>
        get() = _liveInfoOutput
    val liveErrorOutput: StateFlow<String>
        get() = _liveErrorOutput

    private val _liveGrantedDevice = MutableStateFlow<UsbDevice?>(null)
    val liveGrantedDevice: StateFlow<UsbDevice?>
        get() = _liveGrantedDevice

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Constants.ACTION_USB_PERMISSION -> {
                Log.i(TAG, "receiver: USB permission intent received $this")
                val extras = intent.extras
                _liveInfoOutput.value += "\nIntent extras: $extras"

                val device: UsbDevice? = intent.getParcelableExtra(EXTRA_DEVICE)
                val permissionGranted = intent.getBooleanExtra(EXTRA_PERMISSION_GRANTED, false)
                if (permissionGranted) {
                    val info = "${context?.getString(R.string.breceiver_info_usb_permission_granted)} " +
                            "${device?.manufacturerName} ${device?.productName} " +
                            "${device?.vendorId} ${device?.productId}"
                    _liveInfoOutput.value += "\n$info"
                    device?.let {
                        _liveInfoOutput.value = "setting the device globally..."
                        _liveGrantedDevice.value = it
                    }
                } else {
                    val err = "${context?.getString(R.string.breceiver_error_usb_permission_denied)} " +
                            "${device?.manufacturerName} ${device?.productName} " +
                            "${device?.vendorId} ${device?.productId}"
                    _liveErrorOutput.value =
                        "${context?.getString(R.string.breceiver_error_usb_permission_denied)} $err"
                }
            }

            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val attachedMsg = context?.getString(R.string.breceiver_info_device_attached) ?: ""
                _liveOutput.value = attachedMsg
            }

            UsbManager.ACTION_USB_DEVICE_DETACHED -> _liveOutput.value =
                context?.getString(R.string.breceiver_info_device_detached) ?: ""

            null -> {
                val nullMsg = "intent.action was null"
                _liveErrorOutput.value = nullMsg
            }
        }
    }
}