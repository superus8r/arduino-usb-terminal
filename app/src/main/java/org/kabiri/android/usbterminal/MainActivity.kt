package org.kabiri.android.usbterminal

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.sentry.core.Sentry
import kotlinx.android.synthetic.main.activity_main.*
import org.kabiri.android.usbterminal.viewmodel.MainActivityViewModel
import org.koin.android.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // make the text view scrollable:
        tvOutput.movementMethod = ScrollingMovementMethod()

        // open the device and port when the permission is granted by user.
        viewModel.getGrantedDevice().observe(this, Observer { device ->
            viewModel.openDeviceAndPort(device)
        })

        viewModel.getLiveOutput().observe(this, Observer {
            val spannable = SpannableString(it.text)
            spannable.setSpan(
                it.getAppearance(this),
                0,
                it.text.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvOutput.append(it.text)
        })

        // send the command to device when the button is clicked.
        btEnter.setOnClickListener {
            val input = etInput.text.toString()
            if (viewModel.serialWrite(input))
                etInput.setText("") // clear the terminal input.
            else Log.e(TAG, "The message was not sent to Arduino")
        }

        // handle the device mode.
        viewModel.handleDeviceMode(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionConnect -> {
                viewModel.askForConnectionPermission()
                true
            }
            R.id.actionOpenSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
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
