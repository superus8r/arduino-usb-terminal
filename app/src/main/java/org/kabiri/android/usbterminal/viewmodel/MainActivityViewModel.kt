package org.kabiri.android.usbterminal.viewmodel

import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.lifecycle.*
import org.kabiri.android.usbterminal.arduino.ArduinoHelper

/**
 * Created by Ali Kabiri on 12.04.20.
 */
class MainActivityViewModel(val arduinoHelper: ArduinoHelper): ViewModel() {

    fun askForConnectionPermission() = arduinoHelper.askForConnectionPermission()
    fun getGrantedDevice() = arduinoHelper.getGrantedDevice()
    fun openDeviceAndPort(device: UsbDevice) = arduinoHelper.openDeviceAndPort(device)
    fun serialWrite(command: String) = arduinoHelper.serialWrite(command)

    /**
     * Transforms the outputs from ArduinoHelper into spannable text
     * and merges them in one single live data.
     */
    fun getLiveOutput(): LiveData<SpannableString> {

        val liveOutput = arduinoHelper.getLiveOutput()
        val liveInfoOutput = arduinoHelper.getLiveInfoOutput()
        val liveErrorOutput = arduinoHelper.getLiveErrorOutput()

        val liveSpannedOutput: LiveData<SpannableString> = Transformations.map(liveOutput) {
            val spannableString = SpannableString(it)
            spannableString.setSpan(ForegroundColorSpan(Color.DKGRAY), 0, it.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            return@map spannableString
        }

        val liveSpannedInfoOutput: LiveData<SpannableString> = Transformations.map(liveInfoOutput) {
            val spannableString = SpannableString(it)
            spannableString.setSpan(ForegroundColorSpan(Color.BLUE), 0, it.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            return@map spannableString
        }

        val liveSpannedErrorOutput: LiveData<SpannableString> = Transformations.map(liveErrorOutput) {
            val spannableString = SpannableString(it)
            spannableString.setSpan(ForegroundColorSpan(Color.RED), 0, it.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            return@map spannableString
        }

        val liveDataMerger = MediatorLiveData<SpannableString>()
        liveDataMerger.addSource(liveSpannedOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(liveSpannedInfoOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(liveSpannedErrorOutput) { liveDataMerger.value = it }

        return liveDataMerger
    }
}