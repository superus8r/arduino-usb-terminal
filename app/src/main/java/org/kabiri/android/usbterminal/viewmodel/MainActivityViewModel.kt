package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.domain.IArduinoUseCase
import org.kabiri.android.usbterminal.domain.IUsbUseCase
import org.kabiri.android.usbterminal.extensions.IResourceProvider
import org.kabiri.android.usbterminal.extensions.getArduinoType
import org.kabiri.android.usbterminal.extensions.isCloneArduinoBoard
import org.kabiri.android.usbterminal.extensions.isOfficialArduinoBoard
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
    private val usbUseCase: IUsbUseCase,
    private val resourceProvider: IResourceProvider,
): ViewModel() {

    init {
        // Subscribe to USB device changes.
        viewModelScope.launch {
            usbUseCase.usbDevice.collect { device ->
                device?.let { openDeviceAndPort(it) }
            }
        }
    }

    private val _infoMessageFlow = MutableStateFlow("")
    val infoMessage: StateFlow<String>
        get() = _infoMessageFlow

    private val _errorMessageFlow = MutableStateFlow("")
    val errorMessage: StateFlow<String>
        get() = _errorMessageFlow

    private val _outputLive = MutableStateFlow("")
    val output: StateFlow<String>
        get() = _outputLive
    val output2 = SnapshotStateList<OutputText>()

    fun connect() {
        val usbDeviceList = usbUseCase.scanForUsbDevices()
        if (usbDeviceList.isEmpty()) {
            _errorMessageFlow.value =
                resourceProvider.getString(R.string.helper_error_usb_devices_not_attached)
            return // no usb devices found
        }
        val device = usbDeviceList.firstOrNull { it.isOfficialArduinoBoard() || it.isCloneArduinoBoard() }
        if (device == null) {
            _errorMessageFlow.value =
                resourceProvider.getString(R.string.helper_error_arduino_device_not_found)
            _infoMessageFlow.value =
                resourceProvider.getString(R.string.helper_error_connecting_anyway)

            // request permission for the unknown device anyways
            return usbUseCase.requestPermission(usbDeviceList.first())
        }
        when (device.getArduinoType()) {
            OFFICIAL -> {
                usbUseCase.requestPermission(device)
            }

            else -> {
                _infoMessageFlow.value =
                    resourceProvider.getString(R.string.helper_error_connecting_anyway)
                usbUseCase.requestPermission(device)
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
//    suspend fun getLiveOutput(): StateFlow<OutputText> {
//
//        fun Flow<String>.toOutput(type: OutputText.OutputType): Flow<OutputText> =
//            this.map { text ->
//                _outputLive.value += text
//                OutputText(text, type).also { output2.add(it) }
//            }
//
//        // Map each source flow
//        val normalFlow = arduinoUseCase.messageFlow.toOutput(OutputText.OutputType.TYPE_NORMAL)
//        val infoFlow = arduinoUseCase.infoMessageFlow.toOutput(OutputText.OutputType.TYPE_INFO)
//        val errorFlow = arduinoUseCase.errorMessageFlow.toOutput(OutputText.OutputType.TYPE_ERROR)
//
//        // Merge flows into a single stream and expose as StateFlow
//        return merge(normalFlow, infoFlow, errorFlow)
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.Lazily,
//                initialValue = OutputText("", OutputText.OutputType.TYPE_NORMAL)
//            )
//    }
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

        return combine(
            liveSpannedOutput,
            liveSpannedInfoOutput,
            liveSpannedErrorOutput
        ) { normal, info, error ->
            // Prioritize error output over info, then normal.
            when {
                error.text.isNotEmpty() -> error
                info.text.isNotEmpty() -> info
                else -> normal
            }
        }.stateIn(viewModelScope)
    }
}