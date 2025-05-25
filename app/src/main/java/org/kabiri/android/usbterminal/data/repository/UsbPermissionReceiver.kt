package org.kabiri.android.usbterminal.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import org.kabiri.android.usbterminal.Constants

internal class UsbPermissionReceiver(
    private val repository: IUsbRepository,
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Constants.ACTION_USB_PERMISSION) {
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            repository.onPermissionResult(device, granted)
            context?.unregisterReceiver(this)
        }
    }
}