package org.kabiri.android.usbterminal.arduino

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.kabiri.android.usbterminal.ACTION_USB_PERMISSION
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

    private val _liveOutput = MutableLiveData<String>()
    private val _liveInfoOutput = MutableLiveData<String>()
    private val _liveErrorOutput = MutableLiveData<String>()

    val liveOutput: LiveData<String>
        get() = _liveOutput
    val liveInfoOutput: LiveData<String>
        get() = _liveInfoOutput
    val liveErrorOutput: LiveData<String>
        get() = _liveErrorOutput

    private val _liveGrantedDevice = MutableLiveData<UsbDevice>()
    val liveGrantedDevice: LiveData<UsbDevice> // prevent mutable object to be public.
        get() = _liveGrantedDevice

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_USB_PERMISSION -> {

                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                val permissionGranted = intent
                    .getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

                if (permissionGranted) {
                    _liveInfoOutput.postValue(
                        "${context?.getString(R.string.breceiver_info_usb_permission_granted)} ${device?.manufacturerName}"
                    )
                    Log.i(TAG, "USB permission granted by the user")
                    device?.let { _liveGrantedDevice.postValue(it) }
                } else {
                    Log.e(TAG, "USB permission was probably denied by the user")
                    _liveErrorOutput.postValue(
                        "${context?.getString(R.string.breceiver_error_usb_permission_denied)} ${device?.manufacturerName}"
                    )
                }
            }
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> _liveOutput.postValue(context?.getString(R.string.breceiver_info_device_attached))
            UsbManager.ACTION_USB_DEVICE_DETACHED -> _liveOutput.postValue(context?.getString(R.string.breceiver_info_device_detached))
        }
    }
}