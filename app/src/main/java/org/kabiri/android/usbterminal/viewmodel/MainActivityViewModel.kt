package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModel
import org.kabiri.android.usbterminal.arduino.ArduinoHelper

/**
 * Created by Ali Kabiri on 12.04.20.
 */
class MainActivityViewModel(val arduinoHelper: ArduinoHelper): ViewModel() {

//    private val _liveOutput = MutableLiveData<String>()
//    private val _liveInfoOutput = MutableLiveData<String>()
//    private val _liveErrorOutput = MutableLiveData<String>()

    fun askForConnectionPermission() = arduinoHelper.askForConnectionPermission()
    fun getGrantedDevice() = arduinoHelper.getGrantedDevice()
    fun openDeviceAndPort(device: UsbDevice) = arduinoHelper.openDeviceAndPort(device)
    fun serialWrite(command: String) = arduinoHelper.serialWrite(command)
}