package org.kabiri.android.usbterminal.domain

import android.hardware.usb.UsbDevice
import kotlinx.coroutines.flow.Flow
import org.kabiri.android.usbterminal.arduino.ArduinoHelper
import javax.inject.Inject

internal interface IArduinoUseCase {
    fun openDeviceAndPort(device: UsbDevice)
    fun disconnect()
    fun serialWrite(command: String): Boolean
    val output: Flow<String>
    val infoOutput: Flow<String>
    val errorOutput: Flow<String>
}

internal class ArduinoUseCase
@Inject constructor(
    private val arduinoHelper: ArduinoHelper
) : IArduinoUseCase {
    override fun openDeviceAndPort(device: UsbDevice) {
        // Delegate the connection logic to the helper
        arduinoHelper.openDeviceAndPort(device)
    }

    override fun disconnect() {
        arduinoHelper.disconnect()
    }

    override fun serialWrite(command: String): Boolean {
        return arduinoHelper.serialWrite(command)
    }

    override val output get() = arduinoHelper.output

    override val infoOutput get() = arduinoHelper.infoOutput

    override val errorOutput get() = arduinoHelper.errorOutput
}