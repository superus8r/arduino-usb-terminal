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
        // only used in unit tests to make mocking easier.
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

    var deviceModeListener = { _: String -> Unit }
    val deviceModeValue: String?
        get() = mPrefs.getString(mContext.getString(R.string.settings_key_device_mode), null)
    var customServerNameListener = { _: String -> Unit }
    val customServerNameValue: String?
        get() = mPrefs.getString(mContext.getString(R.string.settings_key_custom_device_name), null)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener {
            sharedPrefs, key ->
            if (key == mContext.getString(R.string.settings_key_device_mode)) {
                // device mode changed by the user.
                val mDeviceMode = sharedPrefs?.getString(
                    mContext.getString(R.string.settings_key_device_mode), "") ?: ""
                deviceModeListener.invoke(mDeviceMode)
            }
            if (key == mContext.getString(R.string.settings_key_custom_device_name)) {
                val mServerName = sharedPrefs?.getString(
                    mContext.getString(R.string.settings_key_custom_device_name), "") ?: ""
                customServerNameListener(mServerName)
            }
        }

    private fun registerListener() {
        // register the shared preferences change listener
        mPrefs.registerOnSharedPreferenceChangeListener(listener)
    }
}