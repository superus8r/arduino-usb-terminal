package org.kabiri.android.usbterminal

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import org.kabiri.android.usbterminal.data.SettingsReader
import org.kabiri.android.usbterminal.network.wifi.NsdHelper

/**
 * Created by Ali Kabiri on 12.05.20.
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val settings = SettingsReader(this)
        val helper = NsdHelper()
        settings.discoveryEnabled = {
            Log.d(TAG, "enabled:$it")
            if (it) helper.registerService(this, 999)
            else helper.unregisterService()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}