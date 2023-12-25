package org.kabiri.android.usbterminal.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.kabiri.android.usbterminal.arduino.ArduinoHelper
import org.kabiri.android.usbterminal.arduino.ArduinoPermissionBroadcastReceiver
import org.kabiri.android.usbterminal.arduino.ArduinoSerialReceiver
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import org.kabiri.android.usbterminal.data.repository.USER_SETTING_PREFERENCES_NAME
import org.kabiri.android.usbterminal.data.repository.UserSettingRepository
import org.kabiri.android.usbterminal.domain.GetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.IGetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.ISetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.SetCustomBaudRateUseCase
import javax.inject.Singleton

/**
 * Created by Ali Kabiri on 13.04.20.
 */
@Module
@InstallIn(SingletonComponent::class)
internal class AppModule {

    @Provides
    fun provideContext(
        app: Application
    ): Context = app.applicationContext

    @Provides
    fun provideArduinoPermissionBroadcastReceiver() = ArduinoPermissionBroadcastReceiver()

    @Provides
    fun providesArduinoSerialReceiver() = ArduinoSerialReceiver()

    @Provides
    fun providesArduinoHelper(
        context: Context,
        arduinoPermReceiver: ArduinoPermissionBroadcastReceiver,
        arduinoSerialReceiver: ArduinoSerialReceiver,
        getCustomBaudRateUseCase: IGetCustomBaudRateUseCase,
    ): ArduinoHelper {
        return ArduinoHelper(
            context = context,
            arduinoPermReceiver = arduinoPermReceiver,
            arduinoSerialReceiver = arduinoSerialReceiver,
            getBaudRate = getCustomBaudRateUseCase,
        )
    }

    @Singleton
    @Provides
    fun provideDataStore(
        @ApplicationContext
        context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile(USER_SETTING_PREFERENCES_NAME)
            }
        )
    }

    @Provides
    fun provideUserSettingRepository(
        dataStore: DataStore<Preferences>
    ): IUserSettingRepository {
        return UserSettingRepository(dataStore)
    }

    @Provides
    fun provideGetCustomBaudRateUseCase(
        userSettingRepository: IUserSettingRepository
    ): IGetCustomBaudRateUseCase {
        return GetCustomBaudRateUseCase(userSettingRepository = userSettingRepository)
    }

    @Provides
    fun provideSetCustomBaudRateUseCase(
        userSettingRepository: IUserSettingRepository
    ): ISetCustomBaudRateUseCase {
        return SetCustomBaudRateUseCase(userSettingRepository = userSettingRepository)
    }
}