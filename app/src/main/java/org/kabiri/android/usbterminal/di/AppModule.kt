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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.kabiri.android.usbterminal.arduino.ArduinoRepository
import org.kabiri.android.usbterminal.arduino.ArduinoPermissionBroadcastReceiver
import org.kabiri.android.usbterminal.arduino.ArduinoSerialReceiver
import org.kabiri.android.usbterminal.data.repository.IUsbRepository
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import org.kabiri.android.usbterminal.data.repository.USER_SETTING_PREFERENCES_NAME
import org.kabiri.android.usbterminal.data.repository.UsbRepository
import org.kabiri.android.usbterminal.data.repository.UserSettingRepository
import org.kabiri.android.usbterminal.domain.ArduinoUseCase
import org.kabiri.android.usbterminal.domain.GetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.IArduinoUseCase
import org.kabiri.android.usbterminal.domain.IGetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.ISetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.IUsbUseCase
import org.kabiri.android.usbterminal.domain.SetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.UsbUseCase
import org.kabiri.android.usbterminal.util.IResourceProvider
import org.kabiri.android.usbterminal.util.ResourceProvider
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
    fun provideResourceProvider(
        @ApplicationContext
        context: Context
    ): IResourceProvider = ResourceProvider(context)

    @Provides
    fun provideArduinoPermissionBroadcastReceiver() = ArduinoPermissionBroadcastReceiver()

    @Provides
    fun providesArduinoSerialReceiver() = ArduinoSerialReceiver()

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
        dataStore: DataStore<Preferences>,
    ): IUserSettingRepository {
        return UserSettingRepository(dataStore)
    }

    @Provides
    fun provideUsbRepository(
        context: Context,
    ): IUsbRepository {
        return UsbRepository(
            context = context,
            scope = CoroutineScope(Dispatchers.Default)
        )
    }

    @Provides
    fun providesArduinoRepository(
        context: Context,
        arduinoPermReceiver: ArduinoPermissionBroadcastReceiver,
        arduinoSerialReceiver: ArduinoSerialReceiver,
        getCustomBaudRateUseCase: IGetCustomBaudRateUseCase,
    ): ArduinoRepository {
        return ArduinoRepository(
            context = context,
            arduinoPermReceiver = arduinoPermReceiver,
            arduinoSerialReceiver = arduinoSerialReceiver,
            getBaudRate = getCustomBaudRateUseCase,
        )
    }

    @Provides
    fun provideUsbUseCase(
        usbRepository: IUsbRepository,
    ): IUsbUseCase {
        return UsbUseCase(usbRepository = usbRepository)
    }

    @Provides
    fun provideArduinoUseCase(
        arduinoRepository: ArduinoRepository,
    ): IArduinoUseCase {
        return ArduinoUseCase(arduinoRepository = arduinoRepository)
    }

    @Provides
    fun provideGetCustomBaudRateUseCase(
        userSettingRepository: IUserSettingRepository,
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