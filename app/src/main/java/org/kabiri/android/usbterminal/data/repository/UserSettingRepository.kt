package org.kabiri.android.usbterminal.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.kabiri.android.usbterminal.model.UserSettingPreferences
import org.kabiri.android.usbterminal.model.defaultBaudRate
import java.io.IOException
import javax.inject.Inject

private const val TAG = "UserSettingsRepository"
internal const val USER_SETTING_PREFERENCES_NAME = "user_setting_repository"

private object PreferencesKeys {
    val BAUD_RATE = stringPreferencesKey("baud_rate")
}

internal interface IUserSettingRepository {
    val preferenceFlow: Flow<UserSettingPreferences>
    suspend fun setBaudRate(baudRate: Int)
    suspend fun clear()
    suspend fun fetchInitialPreferences(): UserSettingPreferences
}

internal class UserSettingRepository
@Inject constructor(
    private val dataStore: DataStore<Preferences>
): IUserSettingRepository {
    override val preferenceFlow: Flow<UserSettingPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapPreferences(preferences)
        }

    override suspend fun setBaudRate(baudRate: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BAUD_RATE] = baudRate.toString()
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BAUD_RATE] = defaultBaudRate.toString()
        }
    }

    private fun mapPreferences(preferences: Preferences): UserSettingPreferences {
        // Get the user settings from preferences
        // and convert it to a [UserSettingPreferences] object
        return UserSettingPreferences(
            baudRate = preferences[PreferencesKeys.BAUD_RATE]?.toIntOrNull() ?: defaultBaudRate
        )
    }

    override suspend fun fetchInitialPreferences(): UserSettingPreferences =
        mapPreferences(dataStore.data.first().toPreferences())
}