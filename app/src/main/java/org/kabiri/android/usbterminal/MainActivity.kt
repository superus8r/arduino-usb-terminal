package org.kabiri.android.usbterminal

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.android.synthetic.main.activity_main.*
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }

    private lateinit var usbManager: UsbManager
    private lateinit var connection: UsbDeviceConnection
    private lateinit var serialPort: UsbSerialDevice
    private lateinit var usbReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        usbReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_USB_PERMISSION -> {
                        synchronized(this) {
                            val device: UsbDevice? =
                                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                            if (intent.getBooleanExtra(
                                    UsbManager.EXTRA_PERMISSION_GRANTED,
                                    false
                                )
                            ) {
                                tvOutput.append("\nPermission granted for ${device?.manufacturerName}")
                                device?.apply {
                                    // setup the device communication.
                                    connection = usbManager.openDevice(device)
                                    serialPort = UsbSerialDevice
                                        .createUsbSerialDevice(device, connection)
                                    if (::serialPort.isInitialized) serialPort.let {
                                        if (it.open()) {
                                            // set connection params.
                                            it.setBaudRate(9600)
                                            it.setDataBits(UsbSerialInterface.DATA_BITS_8)
                                            it.setStopBits(UsbSerialInterface.STOP_BITS_1)
                                            it.setParity(UsbSerialInterface.PARITY_NONE)
                                            it.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                                            it.read { message ->
                                                // check if the Android version is not 5.1.1 Lollipop
                                                // before printing the message into output.
                                                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                                                    Log.e(
                                                        TAG,
                                                        "Lollipop 5.1.1 is not supported to show the serial messages from the Arduino."
                                                    )
                                                } else {
                                                    message?.let {
                                                        try {
                                                            val encoded =
                                                                String(
                                                                    message,
                                                                    Charset.defaultCharset()
                                                                )
                                                            tvOutput.append(encoded)
                                                        } catch (e: UnsupportedEncodingException) {
                                                            e.printStackTrace()
                                                            tvOutput
                                                                .append("\n${e.localizedMessage}")
                                                        } catch (e: Exception) {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                e.localizedMessage,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            }
                                            tvOutput.append("\nSerial Connection Opened")
                                        } else {
                                            tvOutput.append("\nPort not opened")
                                        }
                                    } else {
                                        tvOutput.append("\nSerial Port was null")
                                    }

                                }
                            } else {
                                tvOutput.append("\npermission denied for device $device")
                            }
                        }
                    }
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> tvOutput.append("\nDevice attached")
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> tvOutput.append("\nDevice detached")
                }
            }
        }

        btEnter.setOnClickListener {
            val input = etInput.text.toString()
            try {
                if (::serialPort.isInitialized && input.isNotBlank()) {
                    serialPort.write(input.toByteArray())
                    tvOutput.append("\n") // this is because the answer might be sent in more than one part.
                    etInput.setText("") // clear the terminal input.
                } else tvOutput.append("\nSerialPortNotOpened")
            } catch (e: Exception) {
                tvOutput.append("\n${e.localizedMessage}")
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionConnect -> {

                val usbDevices = usbManager.deviceList
                if (usbDevices.isNotEmpty()) {
                    for (device in usbDevices) {
                        val deviceVID = device.value.vendorId
                        if (deviceVID == 0x2341) { // Arduino vendor ID
                            val permissionIntent = PendingIntent.getBroadcast(
                                this,
                                0,
                                Intent(ACTION_USB_PERMISSION),
                                0
                            )
                            val filter = IntentFilter(ACTION_USB_PERMISSION)
                            registerReceiver(usbReceiver, filter) // register the broadcast receiver
                            usbManager.requestPermission(device.value, permissionIntent)
                        } else {
                            tvOutput.append("\nArduino Device not found")
                            connection.close()
                        }
                    }
                } else {
                    tvOutput.append("\nNo USB devices are attached")
                }
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
