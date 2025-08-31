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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.domain.IArduinoUseCase
import org.kabiri.android.usbterminal.domain.IUsbUseCase
import org.kabiri.android.usbterminal.model.ArduinoDevice.ArduinoType.OFFICIAL
import org.kabiri.android.usbterminal.model.OutputText
import org.kabiri.android.usbterminal.util.IResourceProvider
import org.kabiri.android.usbterminal.util.getArduinoType
import org.kabiri.android.usbterminal.util.isCloneArduinoBoard
import org.kabiri.android.usbterminal.util.isOfficialArduinoBoard
import javax.inject.Inject

/**
 * Created by Ali Kabiri on 12.04.20.
 */
@Suppress("ktlint:standard:backing-property-naming")
@HiltViewModel
internal class MainActivityViewModel
    @Inject
    constructor(
        private val arduinoUseCase: IArduinoUseCase,
        private val usbUseCase: IUsbUseCase,
        private val resourceProvider: IResourceProvider,
    ) : ViewModel() {
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

        internal fun startObservingUsbDevice() {
            // Subscribe to USB device changes.
            viewModelScope.launch {
                usbUseCase.usbDevice.collect { device ->
                    _infoMessageFlow.value = "device discovered: ${device?.vendorId}"
                    // TODO: DROID-17 - check if this line is required after DROID-17 is done
                    device?.let { openDeviceAndPort(it) }
                }
            }
        }

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

        fun connectIfAlreadyHasPermission() =
            viewModelScope.launch {
                val usbDevice = usbUseCase.usbDevice.firstOrNull() ?: return@launch
                usbUseCase.hasPermission(usbDevice)
                // TODO: DROID-17 - Fix hasPermission return value not being used here.
                openDeviceAndPort(usbDevice)
            }

        fun disconnect() {
            usbUseCase.disconnect()
            arduinoUseCase.disconnect()
        }

        private fun openDeviceAndPort(device: UsbDevice) =
            viewModelScope.launch {
                arduinoUseCase.openDeviceAndPort(device)
            }

        fun serialWrite(command: String): Boolean {
            _outputLive.value = "${output.value}\n$command"
            val outputText = OutputText(command, OutputText.OutputType.TYPE_INFO)
            output2.add(outputText)
            return arduinoUseCase.serialWrite(command)
        }

        /**
         * Transforms the outputs from ArduinoHelper into spannable text
         * and merges them in one single flow
         */
        suspend fun getLiveOutput(): StateFlow<OutputText> {
            val infoOutput: Flow<OutputText> =
                infoMessage.map {
                    _outputLive.value = _outputLive.value + it
                    val outputText = OutputText(it, OutputText.OutputType.TYPE_INFO)
                    output2.add(outputText)
                    return@map outputText
                }

            val errorOutput: Flow<OutputText> =
                errorMessage.map {
                    _outputLive.value = _outputLive.value + it
                    val outputText = OutputText(it, OutputText.OutputType.TYPE_ERROR)
                    output2.add(outputText)
                    return@map outputText
                }

            val usbInfoOutput: Flow<OutputText> =
                usbUseCase.infoMessageFlow.map {
                    _outputLive.value = _outputLive.value + it
                    val outputText = OutputText(it, OutputText.OutputType.TYPE_INFO)
                    output2.add(outputText)
                    return@map outputText
                }

            val arduinoDefaultOutput: Flow<OutputText> =
                arduinoUseCase.messageFlow.map {
                    _outputLive.value = _outputLive.value + it
                    val outputText = OutputText(it, OutputText.OutputType.TYPE_NORMAL)
                    output2.add(outputText)
                    return@map outputText
                }

            val arduinoInfoOutput: Flow<OutputText> =
                arduinoUseCase.infoMessageFlow.map {
                    _outputLive.value = _outputLive.value + it
                    val outputText = OutputText(it, OutputText.OutputType.TYPE_INFO)
                    output2.add(outputText)
                    return@map outputText
                }

            val arduinoErrorOutput: Flow<OutputText> =
                arduinoUseCase.errorMessageFlow.map {
                    _outputLive.value = _outputLive.value + it
                    val outputText = OutputText(it, OutputText.OutputType.TYPE_ERROR)
                    output2.add(outputText)
                    return@map outputText
                }

            return combine(
                infoOutput,
                errorOutput,
                arduinoDefaultOutput,
                arduinoInfoOutput,
                arduinoErrorOutput,
            ) { info, error, arduinoDefault, arduinoInfo, arduinoError ->
                // Prioritize error output over info, then normal.
                when {
                    error.text.isNotEmpty() -> error
                    info.text.isNotEmpty() -> info
                    arduinoError.text.isNotEmpty() -> arduinoError
                    arduinoInfo.text.isNotEmpty() -> arduinoInfo
                    else -> arduinoDefault
                }
            }.combine(usbInfoOutput) { outputText, usbInfo ->
                // Prioritize USB info output over the rest.
                if (usbInfo.text.isNotEmpty()) {
                    usbInfo
                } else {
                    outputText
                }
            }.stateIn(viewModelScope)
        }
    }
