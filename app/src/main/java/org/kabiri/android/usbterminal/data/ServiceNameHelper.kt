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
        private const val TAG = "ServiceNameHelper"
    }

    lateinit var tPrefs: SharedPreferences // used for mocking in unit tests.
    private val mPrefs: SharedPreferences
        get() {
            return if (::tPrefs.isInitialized) tPrefs
            else PreferenceManager.getDefaultSharedPreferences(context)
        }
    lateinit var serviceName: String

    init {
        // check if there is a uuid available, if not, create one.
        var uuid = mPrefs.getString(KEY_LOCAL_NETWORK_SERVICE_IDENTIFIER, "")
        if (uuid.isNullOrEmpty()) with(mPrefs.edit()) {
            // if there are no uuid, create one and save it.
            uuid = "${Constants.SERVICE_NAME_PREFIX}_${UUID.randomUUID()}"
            putString(KEY_LOCAL_NETWORK_SERVICE_IDENTIFIER, uuid)
            apply()
        }
        uuid?.let {
            serviceName = it
        } ?: run {
            Log.e(TAG, "Service name could not be generated or fetched")
        }
    }
}