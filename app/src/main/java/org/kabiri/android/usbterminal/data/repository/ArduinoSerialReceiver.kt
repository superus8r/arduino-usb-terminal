package org.kabiri.android.usbterminal.data.repository

import android.util.Log
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * Created by Ali Kabiri on 13.04.20.
 *
 * Reads the serial messages from the Arduino.
 */
class ArduinoSerialReceiver: UsbSerialInterface.UsbReadCallback {

    companion object {
        private const val TAG = "ArduinoSerialReceiver"
    }

    private val _liveOutput = MutableStateFlow("")
    private val _liveInfoOutput = MutableStateFlow("")
    private val _liveErrorOutput = MutableStateFlow("")

    val liveOutput: StateFlow<String>
        get() = _liveOutput
    val liveInfoOutput: StateFlow<String>
        get() = _liveInfoOutput
    val liveErrorOutput: StateFlow<String>
        get() = _liveErrorOutput

    override fun onReceivedData(message: ByteArray?) {
        message?.let {
            try { // reading the message from the arduino board.
                val encoded = String(message, Charset.defaultCharset())
                Log.i(TAG, "message from arduino: $encoded")
                _liveOutput.value = encoded
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                Log.e(TAG, "Encoding problem occurred when reading the serial message: $e")
                _liveErrorOutput.value = "\n${e.localizedMessage}"
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error occurred when reading the serial message: $e")
                _liveErrorOutput.value = "\n${e.localizedMessage}"
            }
        } ?: run {
            Log.e(TAG, "Message was null")
        }
    }
}