package org.kabiri.android.usbterminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class JoystickActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
