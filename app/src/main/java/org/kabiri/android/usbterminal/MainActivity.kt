package org.kabiri.android.usbterminal

import android.os.Bundle
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.kabiri.android.usbterminal.viewmodel.MainActivityViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etInput = findViewById<EditText>(R.id.etInput)
        val tvOutput = findViewById<TextView>(R.id.tvOutput)
        val btEnter = findViewById<Button>(R.id.btEnter)

        // make the text view scrollable:
        tvOutput.movementMethod = ScrollingMovementMethod()

        // open the device and port when the permission is granted by user.
        viewModel.getGrantedDevice().observe(this) { device ->
            viewModel.openDeviceAndPort(device)
        }

        viewModel.getLiveOutput().observe(this) {
            val spannable = SpannableString(it.text)
            spannable.setSpan(
                it.getAppearance(this),
                0,
                it.text.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        viewModel.output.observe(this) {
            tvOutput.apply { text = it }
        }

        // send the command to device when the button is clicked.
        btEnter.setOnClickListener {
            val input = etInput.text.toString()
            // append the input to console
            if (viewModel.serialWrite(input))
                etInput.setText("") // clear the terminal input.
            else Log.e(TAG, "The message was not sent to Arduino")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionConnect -> {
                viewModel.askForConnectionPermission()
                true
            }
            R.id.actionDisconnect -> {
                viewModel.disconnect()
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
