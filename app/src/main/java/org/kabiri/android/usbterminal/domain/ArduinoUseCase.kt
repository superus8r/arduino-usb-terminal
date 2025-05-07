package org.kabiri.android.usbterminal.domain

import android.hardware.usb.UsbDevice
import kotlinx.coroutines.flow.Flow
import org.kabiri.android.usbterminal.arduino.ArduinoRepository
import javax.inject.Inject

/**
 * Created by Ali Kabiri on 07.05.2025.
 */

internal interface IArduinoUseCase {
    fun openDeviceAndPort(device: UsbDevice)
    fun disconnect()
    fun serialWrite(command: String): Boolean
    val messageFlow: Flow<String>
    val infoMessageFlow: Flow<String>
    val errorMessageFlow: Flow<String>
}

internal class ArduinoUseCase
@Inject constructor(
    private val arduinoRepository: ArduinoRepository
) : IArduinoUseCase {
    override fun openDeviceAndPort(device: UsbDevice) {
        arduinoRepository.openDeviceAndPort(device)
    }

    override fun disconnect() {
        arduinoRepository.disconnect()
    }

    override fun serialWrite(command: String): Boolean {
        return arduinoRepository.serialWrite(command)
    }

    override val messageFlow get() = arduinoRepository.messageFlow

    override val infoMessageFlow get() = arduinoRepository.infoMessageFlow

    override val errorMessageFlow get() = arduinoRepository.errorMessageFlow
}