package org.kabiri.android.usbterminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import org.kabiri.android.usbterminal.viewmodel.MainActivityViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // open the device and port when the permission is granted by user.
        viewModel.getGrantedDevice().observe(this, Observer { device ->
            viewModel.openDeviceAndPort(device)
        })

        // send the command to device when the button is clicked.
        btEnter.setOnClickListener {
            val input = etInput.text.toString()
            if (viewModel.serialWrite(input))
                etInput.setText("") // clear the terminal input.
            else Log.e(TAG, "Message was not sent to Arduino")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionConnect -> {
                viewModel.askForConnectionPermission()
                true
            }
            R.id.actionOpenJoystick -> {
                val intent = Intent(this, JoystickActivity::class.java)
                startActivity(intent)
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
