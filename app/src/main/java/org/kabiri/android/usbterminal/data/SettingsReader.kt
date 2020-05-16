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

class SettingsReader {
    constructor(context: Context) { mContext = context; registerListener() }
    constructor(context: Context, prefs: SharedPreferences) {
        // only used in unit tests.
        mContext = context
        tPrefs = prefs
        registerListener()
    }

    private lateinit var mContext: Context
    private lateinit var tPrefs: SharedPreferences // used for mocking in unit tests.
    private val mPrefs: SharedPreferences
        get() {
            return if (::tPrefs.isInitialized) tPrefs
            else PreferenceManager.getDefaultSharedPreferences(mContext)
        }

    var discoveryEnabled = { _: Boolean -> Unit }
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener {
            sharedPrefs, key ->
            if (key == mContext.getString(R.string.settings_key_discovery)) {
                // discovery switched on by the user.
                val mDiscovery = sharedPrefs?.getBoolean(
                    mContext.getString(R.string.settings_key_discovery), false) ?: false
                discoveryEnabled(mDiscovery)
            }
        }

    private fun registerListener() {
        // register the shared preferences change listener
        mPrefs.registerOnSharedPreferenceChangeListener(listener)
    }
}