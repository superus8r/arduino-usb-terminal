package org.kabiri.android.usbterminal.domain

import android.hardware.usb.UsbDevice
import kotlinx.coroutines.flow.Flow
import org.kabiri.android.usbterminal.arduino.ArduinoHelper
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
    private val arduinoHelper: ArduinoHelper
) : IArduinoUseCase {
    override fun openDeviceAndPort(device: UsbDevice) {
        arduinoHelper.openDeviceAndPort(device)
    }

    override fun disconnect() {
        arduinoHelper.disconnect()
    }

    override fun serialWrite(command: String): Boolean {
        return arduinoHelper.serialWrite(command)
    }

    override val messageFlow get() = arduinoHelper.messageFlow

    override val infoMessageFlow get() = arduinoHelper.infoMessageFlow

    override val errorMessageFlow get() = arduinoHelper.errorMessageFlow
}