package org.kabiri.android.usbterminal.arduino

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import org.kabiri.android.usbterminal.ACTION_USB_PERMISSION
import org.kabiri.android.usbterminal.R


/**
 * Created by Ali Kabiri on 13.04.20.
 *
 * Helps with sending serial commands to Arduino
 * Info: The `askForConnectionPermission()` method
 *  registers the Arduino Permission Broadcast Receiver.
 */
class ArduinoHelper(private val context: Context,
                    private val arduinoPermReceiver: ArduinoPermissionBroadcastReceiver,
                    private val arduinoSerialReceiver: ArduinoSerialReceiver) {

    companion object {
        private const val TAG = "ArduinoHelper"
    }

    private val _liveOutput = MutableLiveData<String>()
    private val _liveInfoOutput = MutableLiveData<String>()
    private val _liveErrorOutput = MutableLiveData<String>()

    private var usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private lateinit var connection: UsbDeviceConnection
    private lateinit var serialPort: UsbSerialDevice

    /**
     * register the Arduino Permission Broadcast Receiver.
     */
    fun askForConnectionPermission() {
        val usbDevices = usbManager.deviceList
        _liveInfoOutput.postValue(context.getString(R.string.helper_info_checking_attached_usb_devices))
        if (usbDevices.isNotEmpty()) {
            for (device in usbDevices) {
                val deviceVID = device.value.vendorId
                if (deviceVID == 0x2341) { // Arduino vendor ID
                    val permissionIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(ACTION_USB_PERMISSION),
                        0
                    )
                    val filter = IntentFilter(ACTION_USB_PERMISSION)
                    context.registerReceiver(arduinoPermReceiver, filter) // register the broadcast receiver
                    usbManager.requestPermission(device.value, permissionIntent)
                    _liveInfoOutput.postValue(context.getString(R.string.helper_info_usb_permission_requested))
                } else {
                    _liveErrorOutput.postValue(context.getString(R.string.helper_error_device_not_found))
                    connection.close()
                }
            }
        } else {
            _liveErrorOutput.postValue(context.getString(R.string.helper_error_usb_devices_not_attached))
        }
    }

    /**
     * use this live object in activity
     * to call `openDeviceAndPort()` method when the device is available.
      */
    fun getGrantedDevice() = arduinoPermReceiver.liveGrantedDevice

    /**
     * This method should be called after the permission is granted to access the Arduino via USB.
     */
    fun openDeviceAndPort(device: UsbDevice) {
        try {
            // setup the device communication.
            connection = usbManager.openDevice(device)
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "${e.message}")
            _liveErrorOutput.postValue(context.getString(R.string.helper_error_connection_closed_unexpectedly))
        }

        if (::serialPort.isInitialized)
            prepareSerialPort(serialPort)
        else {
            _liveInfoOutput.postValue(context.getString(R.string.helper_error_serial_port_is_null))
            connection.close()
        }
    }

    /**
     * Prepare the serial port to interact with Arduino
     * and register the read callback to read the serial messages from Arduino.
     */
    private fun prepareSerialPort(serialPort: UsbSerialDevice) {
        serialPort.let {
            if (it.open()) {
                // init the serial port and set connection params.
                it.setBaudRate(9600)
                it.setDataBits(UsbSerialInterface.DATA_BITS_8)
                it.setStopBits(UsbSerialInterface.STOP_BITS_1)
                it.setParity(UsbSerialInterface.PARITY_NONE)
                it.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                it.read(arduinoSerialReceiver) // messages will be received from the receiver.
                _liveInfoOutput.postValue(context.getString(R.string.helper_info_serial_connection_opened))
            } else { // serial port not opened.
                _liveErrorOutput.postValue(context.getString(R.string.helper_error_serial_connection_not_opened))
            }
        }
    }

    /**
     * Send a serial message to the connected Arduino.
     */
    fun serialWrite(command: String): Boolean {
        return try {
            if (::serialPort.isInitialized && command.isNotBlank()) {
                serialPort.write(command.toByteArray())
                // go to next line because the answer might be sent in more than one part.
                _liveOutput.postValue(context.getString(R.string.next_line))
                true
            } else {
                _liveErrorOutput.postValue(context.getString(R.string.helper_error_serial_port_is_null))
                false
            }
        } catch (e: Exception) {
            _liveErrorOutput.postValue("${context.getString(R.string.helper_error_write_problem)} ${e.localizedMessage}")
            Log.e(TAG, "$e")
            false
        }
    }

    fun getLiveOutput(): LiveData<String> {

        val liveDataMerger = MediatorLiveData<String>()
        liveDataMerger.addSource(_liveOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(arduinoPermReceiver.liveOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(arduinoSerialReceiver.liveOutput) { liveDataMerger.value = it }

        return liveDataMerger
    }

    fun getLiveInfoOutput(): LiveData<String> {

        val liveDataMerger = MediatorLiveData<String>()
        liveDataMerger.addSource(_liveInfoOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(arduinoPermReceiver.liveInfoOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(arduinoSerialReceiver.liveInfoOutput) { liveDataMerger.value = it }

        return liveDataMerger
    }

    fun getLiveErrorOutput(): LiveData<String> {

        val liveDataMerger = MediatorLiveData<String>()
        liveDataMerger.addSource(_liveErrorOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(arduinoPermReceiver.liveErrorOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(arduinoSerialReceiver.liveErrorOutput) { liveDataMerger.value = it }

        return liveDataMerger
    }

}