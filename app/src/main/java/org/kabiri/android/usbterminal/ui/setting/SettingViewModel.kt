package org.kabiri.android.usbterminal.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.domain.IGetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.ISetCustomBaudRateUseCase
import javax.inject.Inject

/**
 * Created by Ali Kabiri on 22.12.2023.
 */
@HiltViewModel
internal class SettingViewModel
@Inject constructor(
    private val getBaudRate: IGetCustomBaudRateUseCase,
    private val setBaudRate: ISetCustomBaudRateUseCase,
): ViewModel() {

    val currentBaudRate: Flow<Int>
        get() = getBaudRate()

    fun setNewBaudRate(baudRate: Int) = setBaudRate(baudRate)
}