package org.kabiri.android.usbterminal.arduino

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.kabiri.android.usbterminal.Constants

/**
 * Created by Ali Kabiri on 13.04.20.
 *
 * Gets informed when the user has granted the app interaction with Arduino.
 */
class ArduinoPermissionBroadcastReceiver: BroadcastReceiver() {

    companion object {
        private const val TAG = "ArduinoPermReceiver"
    }

    private val liveOutput = MutableLiveData<String>()
    private val liveInfoOutput = MutableLiveData<String>()

    private val _liveGrantedDevice = MutableLiveData<UsbDevice>()
    val liveGrantedDevice: LiveData<UsbDevice> // prevent mutable object to be public.
        get() = _liveGrantedDevice

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Constants.ACTION_USB_PERMISSION -> {

                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                val permissionGranted = intent
                    .getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

                if (permissionGranted) {
                    liveInfoOutput.postValue("\nPermission granted for ${device?.manufacturerName}")
                    Log.i(TAG, "USB permission granted by the user")
                    device?.let { _liveGrantedDevice.postValue(it) }
                } else
                    Log.e(TAG, "USB permission denied by the user")
                    liveInfoOutput.postValue("\npermission denied for device $device")
            }
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> liveOutput.postValue("\nDevice attached")
            UsbManager.ACTION_USB_DEVICE_DETACHED -> liveOutput.postValue("\nDevice detached")
        }
    }
}