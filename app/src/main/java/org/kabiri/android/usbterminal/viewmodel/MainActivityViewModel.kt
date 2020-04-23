package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.kabiri.android.usbterminal.arduino.ArduinoHelper
import org.kabiri.android.usbterminal.model.OutputText

/**
 * Created by Ali Kabiri on 12.04.20.
 */
class MainActivityViewModel(private val arduinoHelper: ArduinoHelper): ViewModel() {

    fun askForConnectionPermission() = arduinoHelper.askForConnectionPermission()
    fun getGrantedDevice() = arduinoHelper.getGrantedDevice()
    fun openDeviceAndPort(device: UsbDevice) = arduinoHelper.openDeviceAndPort(device)
    fun serialWrite(command: String) = arduinoHelper.serialWrite(command)

    /**
     * Transforms the outputs from ArduinoHelper into spannable text
     * and merges them in one single live data.
     */
    fun getLiveOutput(): LiveData<OutputText> {

        val liveOutput = arduinoHelper.getLiveOutput()
        val liveInfoOutput = arduinoHelper.getLiveInfoOutput()
        val liveErrorOutput = arduinoHelper.getLiveErrorOutput()

        val liveSpannedOutput: LiveData<OutputText> = Transformations.map(liveOutput) {
            return@map OutputText(it, OutputText.OutputType.TYPE_NORMAL)
        }

        val liveSpannedInfoOutput: LiveData<OutputText> = Transformations.map(liveInfoOutput) {
            return@map OutputText(it, OutputText.OutputType.TYPE_INFO)
        }

        val liveSpannedErrorOutput: LiveData<OutputText> = Transformations.map(liveErrorOutput) {
            return@map OutputText(it, OutputText.OutputType.TYPE_ERROR)
        }

        val liveDataMerger = MediatorLiveData<OutputText>()
        liveDataMerger.addSource(liveSpannedOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(liveSpannedInfoOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(liveSpannedErrorOutput) { liveDataMerger.value = it }

        return liveDataMerger
    }
}