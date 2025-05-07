package org.kabiri.android.usbterminal.extensions

import android.hardware.usb.UsbDevice

internal fun UsbDevice.isOfficialArduinoBoard(): Boolean {
    val vendorId = this.vendorId
    val productId = this.productId

    // Check if the device is an official Arduino board
    return when (vendorId) {
        0x2341 -> productId in listOf(0x0043, 0x0044, 0x0243, 0x0244, 0x0245, 0x0246, 0x0247)
        0x2A03 -> productId in listOf(0x0001, 0x0002, 0x0003)
        else -> false
    }
}

internal fun UsbDevice.isCloneArduinoBoard(): Boolean {
    val vendorId = this.vendorId
    val productId = this.productId

    // Check if the device is a clone Arduino board
    return when (vendorId) {
        0x1A86 -> productId in listOf(0x7523, 0x7525)
        0x0403 -> productId in listOf(0x6001, 0x6015)
        else -> false
    }
}