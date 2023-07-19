package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val _outputLive = MutableStateFlow("")
    val output: StateFlow<String>
        get() = _outputLive
    val output2 = SnapshotStateList<OutputText>()

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
    suspend fun getLiveOutput(): StateFlow<OutputText> {

        val serialOutput = arduinoHelper.output
        val serialInfoOutput = arduinoHelper.infoOutput
        val serialErrorOutput = arduinoHelper.errorOutput

        val liveSpannedOutput: Flow<OutputText> = serialOutput.map {
            _outputLive.value = _outputLive.value + it
            val outputText = OutputText(it, OutputText.OutputType.TYPE_NORMAL)
            output2.add(outputText)
            return@map outputText
        }

        val liveSpannedInfoOutput: Flow<OutputText> = serialInfoOutput.map {
            _outputLive.value = _outputLive.value + it
            val outputText = OutputText(it, OutputText.OutputType.TYPE_INFO)
            output2.add(outputText)
            return@map outputText
        }

        val liveSpannedErrorOutput: Flow<OutputText> = serialErrorOutput.map {
            _outputLive.value = _outputLive.value + it
            val outputText = OutputText(it, OutputText.OutputType.TYPE_ERROR)
            output2.add(outputText)
            return@map outputText
        }

        return liveSpannedOutput
            .combine(liveSpannedInfoOutput) { a, b -> b }
            .combine(liveSpannedErrorOutput) { a, b -> b }
            .stateIn(viewModelScope)
    }
}