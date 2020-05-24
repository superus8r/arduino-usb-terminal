package org.kabiri.android.usbterminal.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.*

/**
 * Created by Ali Kabiri on 16.05.20.
 *
 * This class wraps up the process of reading the uuid from the Shared Preferences.
 */

class ServiceNameHelper(private val context: Context) {

    companion object {
        private const val KEY_LOCAL_NETWORK_SERVICE_IDENTIFIER = "local_net_service_id"
    }

    lateinit var tPrefs: SharedPreferences // used for mocking in unit tests.
    private val mPrefs: SharedPreferences
        get() {
            return if (::tPrefs.isInitialized) tPrefs
            else PreferenceManager.getDefaultSharedPreferences(context)
        }
    lateinit var tSettings: SettingsReader // used for mocking in tests.
    private val mSettings: SettingsReader
        get() {
            return if (::tSettings.isInitialized) tSettings
            else SettingsReader(context)
        }

    val serviceName: String
        get() {
            val customName = mSettings.customServerNameValue ?: ""
            // create a new identifier and save it.
            return createServiceNameAndSaveFor(customName = customName)
        }

    /**
     * create a new service identifier and save it.
     */
    private fun createServiceNameAndSaveFor(customName: String): String {
        val newServiceName =
            (if (customName.isNotBlank()) "${customName}-" else "") + "${UUID.randomUUID()}"
        with(mPrefs.edit()) {
            // create a new service identifier and save it.
            putString(KEY_LOCAL_NETWORK_SERVICE_IDENTIFIER, newServiceName)
            apply()
        }
        return newServiceName
    }
}