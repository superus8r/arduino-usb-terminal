package org.kabiri.android.usbterminal.arduino

import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.felhr.usbserial.UsbSerialInterface
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

    private val _liveOutput = MutableLiveData<String>()
    private val _liveInfoOutput = MutableLiveData<String>()
    private val _liveErrorOutput = MutableLiveData<String>()

    val liveOutput: LiveData<String>
        get() = _liveOutput
    val liveInfoOutput: LiveData<String>
        get() = _liveInfoOutput
    val liveErrorOutput: LiveData<String>
        get() = _liveErrorOutput

    override fun onReceivedData(message: ByteArray?) {
        // check if the Android version is not 5.1.1 Lollipop
        // before printing the message into output.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1)
            Log.e(
                TAG,
                "Lollipop 5.1.1 is not supported to show the serial messages from the Arduino."
            )
        else { // non lollipop devices:
            message?.let {
                try { // reading the message from the arduino board.
                    val encoded = String(message, Charset.defaultCharset())
                    Log.i(TAG, "message from arduino: $encoded")
                    _liveOutput.postValue(encoded)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    Log.e(TAG, "Encoding problem occurred when reading the serial message: $e")
                    _liveInfoOutput.postValue("\n${e.localizedMessage}")
                } catch (e: Exception) {
                    Log.e(TAG, "Unknown error occurred when reading the serial message: $e")
                    _liveErrorOutput.postValue("\n${e.localizedMessage}")
                }
            }
        }
    }
}