package org.kabiri.android.usbterminal.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import org.kabiri.android.usbterminal.Constants
import java.util.*

/**
 * Created by Ali Kabiri on 16.05.20.
 *
 * This class wraps up the process reading the uuid from the Shared Preferences.
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

    val serviceName: String
        get() {
            // check if there is a service available and return it.
            return mPrefs.getString(KEY_LOCAL_NETWORK_SERVICE_IDENTIFIER, null) ?: run {
                with(mPrefs.edit()) {
                    // create a new name and save it.
                    val newName = "${Constants.SERVICE_NAME_PREFIX}_${UUID.randomUUID()}"
                    putString(KEY_LOCAL_NETWORK_SERVICE_IDENTIFIER, newName)
                    apply()
                    newName
                }
            }
        }
}