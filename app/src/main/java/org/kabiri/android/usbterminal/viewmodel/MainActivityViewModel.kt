package org.kabiri.android.usbterminal.viewmodel

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.SettingsActivity
import org.kabiri.android.usbterminal.arduino.ArduinoHelper
import org.kabiri.android.usbterminal.data.SettingsReader
import org.kabiri.android.usbterminal.model.OutputText
import org.kabiri.android.usbterminal.network.wifi.NsdHelper
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.java.KoinJavaComponent.inject

/**
 * Created by Ali Kabiri on 12.04.20.
 */
class MainActivityViewModel(private val arduinoHelper: ArduinoHelper): ViewModel(), KoinComponent {

    private val settings by inject<SettingsReader>()
    private val mNsdHelper by inject<NsdHelper>()

    fun askForConnectionPermission() = arduinoHelper.askForConnectionPermission()
    fun getGrantedDevice() = arduinoHelper.getGrantedDevice()
    fun openDeviceAndPort(device: UsbDevice) = viewModelScope.launch {
        arduinoHelper.openDeviceAndPort(device)
    }
    fun serialWrite(command: String) = arduinoHelper.serialWrite(command)

    /**
     * Transforms the outputs from ArduinoHelper into spannable text
     * and merges them in one single live data.
     */
    fun getLiveOutput(): LiveData<OutputText> {

        val liveOutput = arduinoHelper.getLiveOutput()
        val liveInfoOutput = arduinoHelper.getLiveInfoOutput()
        val liveErrorOutput = arduinoHelper.getLiveErrorOutput()

        val liveSpannedOutput: LiveData<OutputText> = Transformations.map(liveOutput) {
            return@map OutputText(it, OutputText.OutputType.TYPE_NORMAL)
        }

        val liveSpannedInfoOutput: LiveData<OutputText> = Transformations.map(liveInfoOutput) {
            return@map OutputText(it, OutputText.OutputType.TYPE_INFO)
        }

        val liveSpannedErrorOutput: LiveData<OutputText> = Transformations.map(liveErrorOutput) {
            return@map OutputText(it, OutputText.OutputType.TYPE_ERROR)
        }

        val liveDataMerger = MediatorLiveData<OutputText>()
        liveDataMerger.addSource(liveSpannedOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(liveSpannedInfoOutput) { liveDataMerger.value = it }
        liveDataMerger.addSource(liveSpannedErrorOutput) { liveDataMerger.value = it }

        return liveDataMerger
    }

    /**
     * checks which device mode is selected by the user in the settings and
     * registers and discovers the service if necessary.
     */
    fun handleDeviceMode(context: Context) {
        // handle the service for the currently set mode.
        settings.deviceModeValue?.let {
            handleServiceFor(deviceMode = it, context = context)
        }
        // listen to it for later possible changes.
        settings.deviceModeListener = {
            Log.d(SettingsActivity.TAG, "device mode selected: $it")
            handleServiceFor(deviceMode = it, context = context)
        }
    }

    /**
     * Registers, unregisters, or discovers the Nsd service according to the device mode.
     */
    private fun handleServiceFor(deviceMode: String, context: Context) {
        when (deviceMode) {
            context.getString(R.string.settings_value_device_mode_server) -> {
                mNsdHelper.unregisterService(context)
                mNsdHelper.registerService(context)
            }
            context.getString(R.string.settings_value_device_mode_client) -> {
                mNsdHelper.unregisterService(context)
                mNsdHelper.discoverService(context)
            }
            else -> {
                mNsdHelper.unregisterService(context)
            }
        }
    }
}