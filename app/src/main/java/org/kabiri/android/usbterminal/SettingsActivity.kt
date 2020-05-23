package org.kabiri.android.usbterminal

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.kabiri.android.usbterminal.data.SettingsReader
import org.kabiri.android.usbterminal.network.wifi.NsdHelper
import org.kabiri.android.usbterminal.ui.adapter.WifiDeviceAdapter

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
        // show the wifi device list fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_devices, WifiDeviceListFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val selectedServerPreference: ListPreference =
                findPreference(resources.getString(R.string.settings_key_selected_server))
                    ?: return
            updateDiscoveryData(selectedServerPreference)
        }

        private fun updateDiscoveryData(lp: ListPreference) {
            val entries = arrayOf("Rabbit's Server", "Cat's Server")
            val entryValues = arrayOf("server_rabbit", "server_cat")
            lp.entries = entries
            lp.entryValues = entryValues
        }
    }
}