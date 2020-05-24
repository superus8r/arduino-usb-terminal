package org.kabiri.android.usbterminal.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.*

/**
 * Created by Ali Kabiri on 16.05.20.
 *
 * - This class wraps up the process of reading the uuid from the Shared Preferences.
 * - Basically, there are two parameters saved in the shared prefs,
 *      service_name and service_name_uuid.
 * - The service_name consists of "$custom_name_" + "$service_name_uuid".
 * - The custom name can be empty,
 *      so in that case, the service_name will only be "$service_name_uuid".
 * - Every time the service name is needed,
 *      we have to check if the user has changed the custom_name and return the updated name.
 */

class ServiceNameHelper(private val context: Context) {

    companion object {
        private const val KEY_SERVICE_NAME = "service_name"
        private const val KEY_SERVICE_NAME_UUID = "service_name_uuid"
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
            val serviceNameUuid = mPrefs.getString(KEY_SERVICE_NAME_UUID, "") ?: ""
            val currentServiceName = mPrefs.getString(KEY_SERVICE_NAME, "") ?: ""
            // create a new identifier and save it.
            return getOrCreateServiceNameFor(
                currentServiceName = currentServiceName,
                customName = customName,
                serviceNameUuid = serviceNameUuid
            )
        }

    /**
     * create the service name and save it if needed.
     */
    private fun getOrCreateServiceNameFor(
        currentServiceName: String,
        customName: String,
        serviceNameUuid: String
    ): String {

        // will be initialized if there is currently no uuid (first time to enable discovery)
        var newUuid: String = ""

        val newServiceName =
            (if (customName.isNotBlank()) "${customName}-" else "") +
                    (if (serviceNameUuid.isNotBlank()) serviceNameUuid else {
                        // if the uuid is blank, create a new one and save it.
                        newUuid = UUID.randomUUID().toString()
                        newUuid
                    })

        if (newServiceName != currentServiceName) with(mPrefs.edit()) {

            // if the new uuid is not blank, it means a new uuid is created and we have to
            // save it separately.
            // it can be made during the first time the user activates discovery.
            if (newUuid.isNotBlank()) putString(KEY_SERVICE_NAME_UUID, newUuid)

            // create a new service name and save it.
            putString(KEY_SERVICE_NAME, newServiceName)
            apply()
        }
        return newServiceName
    }
}