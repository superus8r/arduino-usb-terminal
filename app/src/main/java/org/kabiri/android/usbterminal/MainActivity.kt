package org.kabiri.android.usbterminal

import android.os.Bundle
import android.text.SpannableString
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
import org.kabiri.android.usbterminal.extensions.scrollToLastLine
import org.kabiri.android.usbterminal.ui.setting.SettingModalBottomSheet
import org.kabiri.android.usbterminal.ui.setting.SettingViewModel
import org.kabiri.android.usbterminal.viewmodel.MainActivityViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel by viewModels<MainActivityViewModel>()
    private val settingViewModel by viewModels<SettingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // avoid system navbar or soft keyboard overlapping the content.
        val rootView = findViewById<View>(R.id.root_view)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(0, 0, 0, maxOf(systemBarsInsets.bottom, imeInsets.bottom))
            insets
        }

        val etInput = findViewById<EditText>(R.id.etInput)
        val tvOutput = findViewById<TextView>(R.id.tvOutput)
        val btEnter = findViewById<Button>(R.id.btEnter)

        // make the text view scrollable:
        tvOutput.movementMethod = ScrollingMovementMethod()

        lifecycleScope.launchWhenResumed {
            viewModel.getLiveOutput()
        }

        lifecycleScope.launchWhenResumed {
            viewModel.output.collect {
                tvOutput.apply {
                    text = it
                    scrollToLastLine()
                }
            }
        }

        fun sendCommand() {
            val input = etInput.text.toString()
            // append the input to console
            if (viewModel.serialWrite(input))
                etInput.setText("") // clear the terminal input.
            else Log.e(TAG, "The message was not sent to Arduino")
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
            } else false
        }
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
