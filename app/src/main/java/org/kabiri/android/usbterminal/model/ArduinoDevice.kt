package org.kabiri.android.usbterminal.model

import android.hardware.usb.UsbDevice
import org.kabiri.android.usbterminal.util.isCloneArduinoBoard
import org.kabiri.android.usbterminal.util.isOfficialArduinoBoard

internal data class ArduinoDevice(
    val device: UsbDevice,
) {
    val type: ArduinoType = when {
        device.isOfficialArduinoBoard() -> ArduinoType.OFFICIAL
        device.isCloneArduinoBoard() -> ArduinoType.CLONE
        else -> ArduinoType.UNKNOWN
    }
    enum class ArduinoType {
        OFFICIAL, CLONE, UNKNOWN,
    }
}