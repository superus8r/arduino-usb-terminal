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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import org.kabiri.android.usbterminal.extensions.scrollToLastLine
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme
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
        val composeView = findViewById<ComposeView>(R.id.composeView)

        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val columnState = rememberLazyListState()

                UsbTerminalTheme(darkTheme = true) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = columnState,
                        contentPadding = PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        for (item in viewModel.output2) {
                            item {
                                Text(
                                    text = item.text,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                LaunchedEffect(viewModel.output2) {
                                    columnState.animateScrollToItem(viewModel.output2.size-1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // make the text view scrollable:
        tvOutput.movementMethod = ScrollingMovementMethod()

        // open the device and port when the permission is granted by user.
        viewModel.getGrantedDevice().observe(this) { device ->
            viewModel.openDeviceAndPort(device)
        }

        lifecycleScope.launchWhenResumed {
            viewModel.getLiveOutput().collect {
                val spannable = SpannableString(it.text)
                spannable.setSpan(
                    it.getAppearance(this@MainActivity),
                    0,
                    it.text.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.output.collect {
                tvOutput.apply {
                    text = it
                    scrollToLastLine()
                }
            }
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
