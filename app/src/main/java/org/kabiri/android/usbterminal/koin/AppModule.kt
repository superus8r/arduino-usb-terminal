package org.kabiri.android.usbterminal.koin

import org.kabiri.android.usbterminal.arduino.ArduinoHelper
import org.kabiri.android.usbterminal.arduino.ArduinoPermissionBroadcastReceiver
import org.kabiri.android.usbterminal.arduino.ArduinoSerialReceiver
import org.kabiri.android.usbterminal.data.AppDataBase
import org.kabiri.android.usbterminal.data.ServiceNameHelper
import org.kabiri.android.usbterminal.data.SettingsReader
import org.kabiri.android.usbterminal.data.WifiDeviceRepository
import org.kabiri.android.usbterminal.network.wifi.NsdHelper
import org.kabiri.android.usbterminal.viewmodel.MainActivityViewModel
import org.kabiri.android.usbterminal.viewmodel.WifiDeviceListViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by Ali Kabiri on 13.04.20.
 */
val appModule = module {

    single { ArduinoPermissionBroadcastReceiver() }
    single { ArduinoSerialReceiver() }
    single { ArduinoHelper(get(), get(), get()) }
    single { SettingsReader(get()) }
    single { ServiceNameHelper(get()) }
    single { NsdHelper() }
    single { AppDataBase.getInstance(get()) }
    single { WifiDeviceRepository.getInstance((get() as AppDataBase).wifiDeviceDao()) }

    viewModel { MainActivityViewModel(get()) }
    viewModel { WifiDeviceListViewModel(get()) }
}