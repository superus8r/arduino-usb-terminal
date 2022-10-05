package org.kabiri.android.usbterminal

import android.os.Bundle
import androidx.activity.viewModels
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.kabiri.android.usbterminal.databinding.ActivityMainBinding
import org.kabiri.android.usbterminal.viewmodel.MainActivityViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel by viewModels<MainActivityViewModel>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // make the text view scrollable:
        binding.tvOutput.movementMethod = ScrollingMovementMethod()

        // open the device and port when the permission is granted by user.
        viewModel.getGrantedDevice().observe(this, { device ->
            viewModel.openDeviceAndPort(device)
        })

        viewModel.getLiveOutput().observe(this, {
            val spannable = SpannableString(it.text)
            spannable.setSpan(
                it.getAppearance(this),
                0,
                it.text.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvOutput.append(it.text)
        })

        // send the command to device when the button is clicked.
        binding.btEnter.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (viewModel.serialWrite(input))
                binding.etInput.setText("") // clear the terminal input.
            else Log.e(TAG, "The message was not sent to Arduino")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionConnect -> {
                viewModel.askForConnectionPermission()
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
