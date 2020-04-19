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
import org.kabiri.android.usbterminal.Constants


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
        if (usbDevices.isNotEmpty()) {
            for (device in usbDevices) {
                val deviceVID = device.value.vendorId
                if (deviceVID == 0x2341) { // Arduino vendor ID
                    val permissionIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(Constants.ACTION_USB_PERMISSION),
                        0
                    )
                    val filter = IntentFilter(Constants.ACTION_USB_PERMISSION)
                    context.registerReceiver(arduinoPermReceiver, filter) // register the broadcast receiver
                    usbManager.requestPermission(device.value, permissionIntent)
                } else {
                    _liveInfoOutput.postValue("\nArduino Device not found")
                    connection.close()
                }
            }
        } else {
            _liveInfoOutput.postValue("\nNo USB devices are attached")
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
        synchronized(this) { // TODO - probably this should be replaced with coroutines.
            // setup the device communication.
            connection = usbManager.openDevice(device)
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)

            if (::serialPort.isInitialized)
                prepareSerialPort(serialPort)
            else {
                _liveInfoOutput.postValue("\nSerial Port was null")
                connection.close()
            }
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
                _liveInfoOutput.postValue("\nSerial Connection Opened")
            } else { // serial port not opened.
                _liveInfoOutput.postValue("\nPort not opened")
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
                _liveOutput.postValue("\n")
                true
            } else {
                _liveInfoOutput.postValue("Serial Port is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Serial Write encountered an error: $e")
            _liveErrorOutput.postValue("\n${e.localizedMessage}")
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