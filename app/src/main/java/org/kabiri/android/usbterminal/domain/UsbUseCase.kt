package org.kabiri.android.usbterminal.domain

import android.content.Context
import android.hardware.usb.UsbDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.kabiri.android.usbterminal.data.repository.IUsbRepository
import javax.inject.Inject

internal interface IUsbUseCase {
    val usbDevice: SharedFlow<UsbDevice?>
    val infoMessageFlow: Flow<String>
    fun scanForUsbDevices(activityContext: Context): List<UsbDevice>
    fun hasPermission(activityContext: Context, device: UsbDevice): Boolean
    fun requestPermission(activityContext: Context, device: UsbDevice)
    fun disconnect()
}

internal class UsbUseCase
@Inject constructor(
    private val usbRepository: IUsbRepository
): IUsbUseCase {
    override val usbDevice: SharedFlow<UsbDevice?> = usbRepository.usbDevice

    override val infoMessageFlow: Flow<String> = usbRepository.infoMessageFlow

    override fun scanForUsbDevices(activityContext: Context): List<UsbDevice> = usbRepository.scanForArduinoDevices(activityContext)

    override fun hasPermission(activityContext: Context, device: UsbDevice): Boolean = usbRepository.hasPermission(activityContext, device)

    override fun requestPermission(activityContext: Context, device: UsbDevice) = usbRepository.requestUsbPermission(activityContext, device)

    override fun disconnect() = usbRepository.disconnect()
}