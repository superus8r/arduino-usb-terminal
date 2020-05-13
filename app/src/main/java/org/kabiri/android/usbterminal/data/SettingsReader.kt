package org.kabiri.android.usbterminal.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.kabiri.android.usbterminal.R

/**
 * Created by Ali Kabiri on 12.05.20.
 *
 * This class wraps up the process of listening to shared preferences callback
 * using a simple lambda.
 */

class SettingsReader(private val context: Context) {
    var discoveryEnabled = { _: Boolean -> Unit }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener {
            sharedPrefs, key ->
            if (key == context.getString(R.string.settings_key_discovery)) {
                val mDiscovery = sharedPrefs?.getBoolean(
                    context.getString(R.string.settings_key_discovery),
                    false) ?: false
                discoveryEnabled(mDiscovery)
            }
        }
    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }
}