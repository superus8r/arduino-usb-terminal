package org.kabiri.android.usbterminal.domain

import android.hardware.usb.UsbDevice
import kotlinx.coroutines.flow.StateFlow
import org.kabiri.android.usbterminal.data.repository.IUsbRepository
import org.kabiri.android.usbterminal.model.ArduinoDevice
import javax.inject.Inject

internal interface IUsbUseCase {
    val usbDevice: StateFlow<UsbDevice?>
    fun scanForArduinoDevices(onResult: (device: ArduinoDevice?) -> Unit)
    fun requestPermission(device: UsbDevice)
    fun disconnect()
}

internal class UsbUseCase
@Inject constructor(
    private val usbRepository: IUsbRepository
): IUsbUseCase {
    override val usbDevice: StateFlow<UsbDevice?> get() = usbRepository.usbDevice

    override fun scanForArduinoDevices(onResult: (ArduinoDevice?) -> Unit) = usbRepository.scanForArduinoDevices(onResult)

    override fun requestPermission(device: UsbDevice) = usbRepository.requestUsbPermission(device)

    override fun disconnect() = usbRepository.disconnect()
}