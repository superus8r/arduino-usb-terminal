package org.kabiri.android.usbterminal

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.ui.setting.SettingModalBottomSheet
import org.kabiri.android.usbterminal.ui.setting.SettingViewModel
import org.kabiri.android.usbterminal.util.scrollToLastLine
import org.kabiri.android.usbterminal.viewmodel.MainActivityViewModel

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()
    private val settingViewModel by viewModels<SettingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startObservingUsbDevice()
        setContentView(R.layout.activity_main)

        // avoid system navbar or soft keyboard overlapping the content.
        val rootView = findViewById<View>(R.id.root_view)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(0, systemBarsInsets.top, 0, maxOf(systemBarsInsets.bottom, imeInsets.bottom))
            insets
        }

        val etInput = findViewById<EditText>(R.id.etInput)
        val tvOutput = findViewById<TextView>(R.id.tvOutput)
        val btEnter = findViewById<Button>(R.id.btEnter)

        // make the text view scrollable:
        tvOutput.movementMethod = ScrollingMovementMethod()

        var autoScrollEnabled = true
        lifecycleScope.launch {
            settingViewModel.currentAutoScroll.collect { enabled ->
                autoScrollEnabled = enabled
            }
        }

        lifecycleScope.launch {
            viewModel.getLiveOutput()
            viewModel.output.collect {
                tvOutput.apply {
                    text = it
                    if (autoScrollEnabled) scrollToLastLine()
                }
            }
        }

        fun sendCommand() {
            val input = etInput.text.toString()
            // append the input to console
            if (viewModel.serialWrite(input)) {
                etInput.setText("")
            } else {
                Log.e(TAG, "The message was not sent to Arduino")
            }
        }

        // send the command to device when the button is clicked.
        btEnter.setOnClickListener {
            sendCommand()
        }

        etInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_DOWN)) {
                sendCommand()
                true
            } else {
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectIfAlreadyHasPermission()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionConnect -> {
                viewModel.connect()
                true
            }
            R.id.actionDisconnect -> {
                viewModel.disconnect()
                true
            }
            R.id.actionSettings -> {
                SettingModalBottomSheet(viewModel = settingViewModel)
                    .show(supportFragmentManager, TAG)
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
