package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.arduino.ArduinoHelper
import org.kabiri.android.usbterminal.model.OutputText
import javax.inject.Inject

/**
 * Created by Ali Kabiri on 12.04.20.
 */
@HiltViewModel
class MainActivityViewModel
@Inject constructor(
    private val arduinoHelper: ArduinoHelper,
): ViewModel() {

    private val _outputLive = MutableLiveData("")
    val output = _outputLive

    fun askForConnectionPermission() = arduinoHelper.askForConnectionPermission()

    fun disconnect() = arduinoHelper.disconnect()
    fun getGrantedDevice() = arduinoHelper.getGrantedDevice()
    fun openDeviceAndPort(device: UsbDevice) = viewModelScope.launch {
        arduinoHelper.openDeviceAndPort(device)
    }
    fun serialWrite(command: String): Boolean {
        _outputLive.value = "${output.value}\n$command\n"
        return arduinoHelper.serialWrite(command)
    }

    /**
     * Transforms the outputs from ArduinoHelper into spannable text
     * and merges them in one single live data.
     */
    fun getLiveOutput(): LiveData<OutputText> {

        val liveOutput = arduinoHelper.getLiveOutput()
        val liveInfoOutput = arduinoHelper.getLiveInfoOutput()
        val liveErrorOutput = arduinoHelper.getLiveErrorOutput()

        val liveSpannedOutput: LiveData<OutputText> = Transformations.map(liveOutput) {
            _outputLive.value = _outputLive.value + it
            return@map OutputText(it, OutputText.OutputType.TYPE_NORMAL)
        }

        val liveSpannedInfoOutput: LiveData<OutputText> = Transformations.map(liveInfoOutput) {
            _outputLive.value = _outputLive.value + it
            return@map OutputText(it, OutputText.OutputType.TYPE_INFO)
        }

        val liveSpannedErrorOutput: LiveData<OutputText> = Transformations.map(liveErrorOutput) {
            _outputLive.value = _outputLive.value + it
            return@map OutputText(it, OutputText.OutputType.TYPE_ERROR)
        }

        val liveDataMerger = MediatorLiveData<OutputText>()
        liveDataMerger.addSource(liveSpannedOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(liveSpannedInfoOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(liveSpannedErrorOutput) { liveDataMerger.value = it }

        return liveDataMerger
    }
}