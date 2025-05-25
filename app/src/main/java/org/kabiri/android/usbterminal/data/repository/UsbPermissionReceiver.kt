package org.kabiri.android.usbterminal.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import org.kabiri.android.usbterminal.Constants

internal class UsbPermissionReceiver(
    private val repository: IUsbRepository,
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Constants.ACTION_USB_PERMISSION -> {
                val device = intent.getUsbDeviceExtra()
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                repository.onPermissionResult(device, granted)
                context?.unregisterReceiver(this)
            }

            UsbManager.ACTION_USB_DEVICE_ATTACHED ->
                repository.onDeviceAttached(intent.getUsbDeviceExtra())

            UsbManager.ACTION_USB_DEVICE_DETACHED ->
                repository.onDeviceAttached(intent.getUsbDeviceExtra())

            null -> repository.onUnknownAction(intent)
        }
    }
}

private fun Intent.getUsbDeviceExtra(): UsbDevice? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(UsbManager.EXTRA_DEVICE)
    }
