package org.kabiri.android.usbterminal.arduino

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.Constants
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.domain.IGetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.model.defaultBaudRate
import javax.inject.Inject


/**
 * Created by Ali Kabiri on 13.04.20.
 *
 * Helps with sending serial commands to Arduino
 * Info: The `askForConnectionPermission()` method
 *  registers the Arduino Permission Broadcast Receiver.
 */

private const val TAG = "ArduinoHelper"

internal class ArduinoHelper
@Inject constructor(
    private val context: Context,
    private val arduinoPermReceiver: ArduinoPermissionBroadcastReceiver,
    private val arduinoSerialReceiver: ArduinoSerialReceiver,
    private val getBaudRate: IGetCustomBaudRateUseCase,
) {

    private var currentBaudRate = defaultBaudRate // Default value

    private val _liveOutput = MutableStateFlow("")
    val output: Flow<String>
        get() = _liveOutput
            .combine(arduinoPermReceiver.liveOutput) { a, b -> a + b }
            .combine(arduinoSerialReceiver.liveOutput) { a, b -> a + b }

    private val _liveInfoOutput = MutableStateFlow("")
    val infoOutput: Flow<String>
        get() = _liveInfoOutput
            .combine(arduinoPermReceiver.liveInfoOutput) { a, b -> a + b }
            .combine(arduinoSerialReceiver.liveInfoOutput) { a, b -> a + b }

    private val _liveErrorOutput = MutableStateFlow("")
    val errorOutput: Flow<String>
        get() = _liveErrorOutput
            .combine(arduinoPermReceiver.liveErrorOutput) { a, b -> a + b }
            .combine(arduinoSerialReceiver.liveErrorOutput) { a, b -> a + b }

    private var usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private lateinit var connection: UsbDeviceConnection
    private lateinit var serialPort: UsbSerialDevice

    init {
        observeBaudRate()
    }

    /**
     * register the Arduino Permission Broadcast Receiver.
     */
    fun askForConnectionPermission() {
        val usbDevices = usbManager.deviceList
        _liveInfoOutput.value =
            context.getString(R.string.helper_info_checking_attached_usb_devices)
        if (usbDevices.isNotEmpty()) {
            for (device in usbDevices) {
                val deviceVID = device.value.vendorId
                if (deviceVID == 0x2341) { // Arduino vendor ID
                    connect(device)
                } else {
                    _liveErrorOutput.value =
                        context.getString(R.string.helper_error_device_not_found)
                    _liveErrorOutput.value =
                        context.getString(R.string.helper_error_connecting_anyway)
                    connect(device)
                }
            }
        } else {
            _liveErrorOutput.value = ""
            _liveErrorOutput.value =
                context.getString(R.string.helper_error_usb_devices_not_attached)
        }
    }

    fun disconnect() {
        try {
            if (::connection.isInitialized) connection.close()
            _liveOutput.value = context.getString(R.string.helper_info_serial_connection_closed)
        } catch (e: UninitializedPropertyAccessException) {
            _liveErrorOutput.value =
                context.getString(R.string.helper_error_connection_not_ready_to_close)

            _liveErrorOutput.value = "${e.localizedMessage}\n"
        } catch (e: Exception) {
            _liveErrorOutput.value = context.getString(
                    R.string.helper_error_connection_failed_to_close
                )
            _liveErrorOutput.value = "${e.localizedMessage}\n"
        }
    }

    private fun connect(device: MutableMap.MutableEntry<String, UsbDevice>) {
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(Constants.ACTION_USB_PERMISSION),
            // it is necessary for connecting to the device.
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val filter = IntentFilter(Constants.ACTION_USB_PERMISSION)
        context.registerReceiver(arduinoPermReceiver, filter) // register the broadcast receiver
        usbManager.requestPermission(device.value, permissionIntent)
        _liveInfoOutput.value = context.getString(R.string.helper_info_usb_permission_requested)
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
            _liveErrorOutput.value =
                context.getString(R.string.helper_error_connection_closed_unexpectedly)
        } catch (e: NullPointerException) {
            Log.e(TAG, "${e.message}")
            _liveErrorOutput.value = context.getString(
                R.string.helper_error_connection_failed_to_open)
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
            _liveErrorOutput.value =
                context.getString(R.string.helper_error_connection_failed_to_open_unknown)
        }

        if (::serialPort.isInitialized)
            prepareSerialPort(serialPort)
        else {
            _liveInfoOutput.value = context.getString(R.string.helper_error_serial_port_is_null)
            if (::connection.isInitialized) connection.close()
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
                Log.d(TAG, "current baud rate = $currentBaudRate")
                it.setBaudRate(currentBaudRate)
                it.setDataBits(UsbSerialInterface.DATA_BITS_8)
                it.setStopBits(UsbSerialInterface.STOP_BITS_1)
                it.setParity(UsbSerialInterface.PARITY_NONE)
                it.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                it.read(arduinoSerialReceiver) // messages will be received from the receiver.
                _liveInfoOutput.value =
                    context.getString(R.string.helper_info_serial_connection_opened)
            } else { // serial port not opened.
                _liveErrorOutput.value =
                    context.getString(R.string.helper_error_serial_connection_not_opened)
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
                _liveOutput.value = context.getString(R.string.next_line)
                true
            } else {
                _liveErrorOutput.value =
                    context.getString(R.string.helper_error_serial_port_is_null)
                false
            }
        } catch (e: Exception) {
            _liveErrorOutput.value = context.getString(R.string.helper_error_write_problem) +
                    " \n${e.localizedMessage}\n"
            Log.e(TAG, "$e")
            false
        }
    }

    /**
     * Listen for changes in baudRate set by the user
     */
    private fun observeBaudRate() {
        CoroutineScope(Dispatchers.IO).launch {
            getBaudRate().collect { baudRate ->
                currentBaudRate = baudRate
                _liveInfoOutput.value = String.format(
                    context.getString(R.string.helper_info_baud_rate_applying), baudRate)
                if (::serialPort.isInitialized) {
                    updateSerialPortBaudRate(baudRate)
                    _liveInfoOutput.value = String.format(
                        context.getString(R.string.helper_info_baud_rate_applied), baudRate)
                } else {
                    _liveErrorOutput.value =
                        context.getString(R.string.helper_error_baud_rate_failed_to_apply_no_connection)
                }
            }
        }
    }

    /**
     * Update value of current baud rate to new one from the user
     */
    private fun updateSerialPortBaudRate(baudRate: Int) {
        try {
            serialPort.setBaudRate(baudRate)
        } catch (e: Exception) {
            _liveErrorOutput.value = context.getString(R.string.helper_error_applying_baud_rate)
        }
    }
}