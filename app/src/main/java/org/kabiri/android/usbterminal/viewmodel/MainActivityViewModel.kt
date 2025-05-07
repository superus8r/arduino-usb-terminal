package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.domain.IArduinoUseCase
import org.kabiri.android.usbterminal.domain.IUsbUseCase
import org.kabiri.android.usbterminal.model.ArduinoDevice.ArduinoType.CLONE
import org.kabiri.android.usbterminal.model.ArduinoDevice.ArduinoType.OFFICIAL
import org.kabiri.android.usbterminal.model.OutputText
import javax.inject.Inject

/**
 * Created by Ali Kabiri on 12.04.20.
 */
@HiltViewModel
internal class MainActivityViewModel
@Inject constructor(
    private val arduinoUseCase: IArduinoUseCase,
    private val usbUseCase: IUsbUseCase
): ViewModel() {

    init {
        // Subscribe to USB device changes.
        viewModelScope.launch {
            usbUseCase.usbDevice.collect { device ->
                device?.let { openDeviceAndPort(it) }
            }
        }
    }

    private val _outputLive = MutableStateFlow("")
    val output: StateFlow<String>
        get() = _outputLive
    val output2 = SnapshotStateList<OutputText>()

    fun connect() {
        usbUseCase.scanForArduinoDevices { result ->
            if (result == null) return@scanForArduinoDevices
            when (result.type) {
                OFFICIAL -> {
//                    _liveErrorOutput.value =
//                        context.getString(R.string.helper_error_device_not_found)
//                    _liveErrorOutput.value =
//                        context.getString(R.string.helper_error_connecting_anyway)
                    usbUseCase.requestPermission(result.device)
                }
                CLONE -> {
//                    _liveErrorOutput.value = ""
//                    _liveErrorOutput.value =
//                        context.getString(R.string.helper_error_usb_devices_not_attached)
                    usbUseCase.requestPermission(result.device)
                }
                else -> {

                }
            }
        }
    }

    fun disconnect() {
        usbUseCase.disconnect()
        arduinoUseCase.disconnect()
    }

    fun openDeviceAndPort(device: UsbDevice) = viewModelScope.launch {
        arduinoUseCase.openDeviceAndPort(device)
    }

    fun serialWrite(command: String): Boolean {
        _outputLive.value = "${output.value}\n$command\n"
        return arduinoUseCase.serialWrite(command)
    }

    /**
     * Transforms the outputs from ArduinoHelper into spannable text
     * and merges them in one single flow
     */
    suspend fun getLiveOutput(): StateFlow<OutputText> {

        val serialOutput = arduinoUseCase.messageFlow
        val serialInfoOutput = arduinoUseCase.infoMessageFlow
        val serialErrorOutput = arduinoUseCase.errorMessageFlow

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