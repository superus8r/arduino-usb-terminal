package org.kabiri.android.usbterminal.arduino

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kabiri.android.usbterminal.Constants
import org.kabiri.android.usbterminal.R

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

    private val _liveGrantedDevice = MutableLiveData<UsbDevice>()
    val liveGrantedDevice: LiveData<UsbDevice> // prevent mutable object to be public.
        get() = _liveGrantedDevice

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Constants.ACTION_USB_PERMISSION -> {

                val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                }
                val permissionGranted = intent
                    .getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

                if (permissionGranted) {
                    _liveInfoOutput.value =
                        "${context?.getString(R.string.breceiver_info_usb_permission_granted)} ${device?.manufacturerName}"
                    Log.i(TAG, "USB permission granted by the user")
                    device?.let { _liveGrantedDevice.postValue(it) }
                } else {
                    Log.e(TAG, "USB permission was probably denied by the user")
                    _liveErrorOutput.value =
                        "${context?.getString(R.string.breceiver_error_usb_permission_denied)} ${device?.manufacturerName}"
                }
            }
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> _liveOutput.value =
                context?.getString(R.string.breceiver_info_device_attached) ?: ""
            UsbManager.ACTION_USB_DEVICE_DETACHED -> _liveOutput.value =
                context?.getString(R.string.breceiver_info_device_detached) ?: ""
        }
    }
}