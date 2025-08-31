package org.kabiri.android.usbterminal

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import org.kabiri.android.usbterminal.ui.setting.SettingModalBottomSheet
import org.kabiri.android.usbterminal.ui.setting.SettingViewModel
import org.kabiri.android.usbterminal.ui.terminal.TerminalOutput
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme
import org.kabiri.android.usbterminal.viewmodel.MainActivityViewModel

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()
    private val settingViewModel by viewModels<SettingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startObservingUsbDevice()
        viewModel.startObservingTerminalOutput()
        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(R.id.root_view)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            // Toolbar consumes status bar space; content avoids bottom system bars
            toolbar.setPadding(
                toolbar.paddingLeft,
                systemBarsInsets.top,
                toolbar.paddingRight,
                toolbar.paddingBottom,
            )
            view.setPadding(0, 0, 0, maxOf(systemBarsInsets.bottom, imeInsets.bottom))
            insets
        }

        val etInput = findViewById<EditText>(R.id.etInput)
        val composeOutput = findViewById<ComposeView>(R.id.composeOutput)
        val btEnter = findViewById<Button>(R.id.btEnter)

        // Compose terminal output UI
        composeOutput.setContent {
            UsbTerminalTheme {
                val autoScrollEnabled = settingViewModel.currentAutoScroll.collectAsState(initial = true).value
                TerminalOutput(
                    logs = viewModel.output2,
                    autoScroll = autoScrollEnabled,
                )
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
