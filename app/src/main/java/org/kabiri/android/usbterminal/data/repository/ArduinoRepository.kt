package org.kabiri.android.usbterminal.data.repository

import android.content.Context
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

private const val TAG = "ArduinoRepository"

interface IArduinoRepository {
    val messageFlow: Flow<String>
    val infoMessageFlow: Flow<String>
    val errorMessageFlow: Flow<String>
    fun disconnect()
    fun openDeviceAndPort(device: UsbDevice)
    fun serialWrite(command: String): Boolean
}

internal class ArduinoRepository
@Inject constructor(
    private val context: Context,
    private val arduinoSerialReceiver: ArduinoSerialReceiver,
    private val getBaudRate: IGetCustomBaudRateUseCase,
): IArduinoRepository {

    private var currentBaudRate = defaultBaudRate // Default value

    private val _messageFlow = MutableStateFlow("")
    override val messageFlow: Flow<String>
        get() = _messageFlow
            .combine(arduinoSerialReceiver.liveOutput) { a, b -> a + b }

    private val _infoMessageFlow = MutableStateFlow("")
    override val infoMessageFlow: Flow<String>
        get() = _infoMessageFlow
            .combine(arduinoSerialReceiver.liveInfoOutput) { a, b -> a + b }

    private val _errorMessageFlow = MutableStateFlow("")
    override val errorMessageFlow: Flow<String>
        get() = _errorMessageFlow
            .combine(arduinoSerialReceiver.liveErrorOutput) { a, b -> a + b }

    private var usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private lateinit var connection: UsbDeviceConnection
    private lateinit var serialPort: UsbSerialDevice

    init {
        observeBaudRate()
    }

    override fun disconnect() {
        try {
            if (::connection.isInitialized) connection.close()
            _messageFlow.value = context.getString(R.string.helper_info_serial_connection_closed)
        } catch (e: UninitializedPropertyAccessException) {
            _errorMessageFlow.value =
                context.getString(R.string.helper_error_connection_not_ready_to_close)

            _errorMessageFlow.value = "${e.localizedMessage}\n"
        } catch (e: Exception) {
            _errorMessageFlow.value = context.getString(
                    R.string.helper_error_connection_failed_to_close
                )
            _errorMessageFlow.value = "${e.localizedMessage}\n"
        }
    }

    /**
     * This method should be called after the permission is granted to access the Arduino via USB.
     */
    override fun openDeviceAndPort(device: UsbDevice) {
        try {
            // setup the device communication.
            connection = usbManager.openDevice(device)
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "${e.message}")
            _errorMessageFlow.value =
                context.getString(R.string.helper_error_connection_closed_unexpectedly)
        } catch (e: NullPointerException) {
            Log.e(TAG, "${e.message}")
            _errorMessageFlow.value = context.getString(
                R.string.helper_error_connection_failed_to_open)
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
            _errorMessageFlow.value =
                context.getString(R.string.helper_error_connection_failed_to_open_unknown)
        }

        if (::serialPort.isInitialized)
            prepareSerialPort(serialPort)
        else {
            _infoMessageFlow.value = context.getString(R.string.helper_error_serial_port_is_null)
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
                _infoMessageFlow.value =
                    context.getString(R.string.helper_info_serial_connection_opened)
            } else { // serial port not opened.
                _errorMessageFlow.value =
                    context.getString(R.string.helper_error_serial_connection_not_opened)
            }
        }
    }

    /**
     * Send a serial message to the connected Arduino.
     */
    override fun serialWrite(command: String): Boolean {
        return try {
            if (::serialPort.isInitialized && command.isNotBlank()) {
                serialPort.write(command.toByteArray())
                // go to next line because the answer might be sent in more than one part.
                _messageFlow.value = context.getString(R.string.next_line)
                true
            } else {
                _errorMessageFlow.value =
                    context.getString(R.string.helper_error_serial_port_is_null)
                false
            }
        } catch (e: Exception) {
            _errorMessageFlow.value = context.getString(R.string.helper_error_write_problem) +
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
                _infoMessageFlow.value = String.format(
                    context.getString(R.string.helper_info_baud_rate_applying), baudRate)
                if (::serialPort.isInitialized) {
                    updateSerialPortBaudRate(baudRate)
                    _infoMessageFlow.value = String.format(
                        context.getString(R.string.helper_info_baud_rate_applied), baudRate)
                } else {
                    _errorMessageFlow.value =
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
            _errorMessageFlow.value = context.getString(R.string.helper_error_applying_baud_rate) +
                    " \n${e.localizedMessage}"
        }
    }
}