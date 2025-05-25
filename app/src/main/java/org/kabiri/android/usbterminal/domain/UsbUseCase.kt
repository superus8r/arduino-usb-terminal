package org.kabiri.android.usbterminal.domain

import android.hardware.usb.UsbDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.kabiri.android.usbterminal.data.repository.IUsbRepository
import javax.inject.Inject

internal interface IUsbUseCase {
    val usbDevice: SharedFlow<UsbDevice?>
    val infoMessageFlow: Flow<String>
    fun scanForUsbDevices(): List<UsbDevice>
    fun requestPermission(device: UsbDevice)
    fun disconnect()
}

internal class UsbUseCase
@Inject constructor(
    private val usbRepository: IUsbRepository
): IUsbUseCase {
    override val usbDevice: SharedFlow<UsbDevice?> = usbRepository.usbDevice

    override val infoMessageFlow: Flow<String> = usbRepository.infoMessageFlow

    override fun scanForUsbDevices(): List<UsbDevice> = usbRepository.scanForArduinoDevices()

    override fun requestPermission(device: UsbDevice) = usbRepository.requestUsbPermission(device)

    override fun disconnect() = usbRepository.disconnect()
}