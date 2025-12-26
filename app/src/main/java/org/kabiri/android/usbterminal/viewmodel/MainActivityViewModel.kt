package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
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

        val output = SnapshotStateList<OutputText>()

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
            val device =
                usbDeviceList.firstOrNull { it.isOfficialArduinoBoard() || it.isCloneArduinoBoard() }
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
            val outputText = OutputText(command, OutputText.OutputType.TYPE_NORMAL)
            output.add(outputText)
            return arduinoUseCase.serialWrite(command)
        }

        /**
         * Starts emitting all output sources to the snapshot list used by the UI.
         * Emits every item (including repeats) with its type.
         */
        fun startObservingTerminalOutput() {
            val infoOutput: Flow<OutputText> =
                infoMessage
                    .filter { it.isNotEmpty() }
                    .map { OutputText(it, OutputText.OutputType.TYPE_INFO) }

            val errorOutput: Flow<OutputText> =
                errorMessage
                    .filter { it.isNotEmpty() }
                    .map { OutputText(it, OutputText.OutputType.TYPE_ERROR) }

            val usbInfoOutput: Flow<OutputText> =
                usbUseCase.infoMessageFlow
                    .filter { it.isNotEmpty() }
                    .map { OutputText(it, OutputText.OutputType.TYPE_INFO) }

            val arduinoDefaultOutput: Flow<OutputText> =
                arduinoUseCase.messageFlow
                    .filter { it.isNotEmpty() }
                    .map { OutputText(it, OutputText.OutputType.TYPE_NORMAL) }

            val arduinoInfoOutput: Flow<OutputText> =
                arduinoUseCase.infoMessageFlow
                    .filter { it.isNotEmpty() }
                    .map { OutputText(it, OutputText.OutputType.TYPE_INFO) }

            val arduinoErrorOutput: Flow<OutputText> =
                arduinoUseCase.errorMessageFlow
                    .filter { it.isNotEmpty() }
                    .map { OutputText(it, OutputText.OutputType.TYPE_ERROR) }

            merge(
                infoOutput,
                errorOutput,
                usbInfoOutput,
                arduinoDefaultOutput,
                arduinoInfoOutput,
                arduinoErrorOutput,
            ).onEach { output.add(it) }
                .launchIn(viewModelScope)
        }
    }
