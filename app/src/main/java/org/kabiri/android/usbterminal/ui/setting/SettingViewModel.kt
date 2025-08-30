package org.kabiri.android.usbterminal.ui.setting

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import org.kabiri.android.usbterminal.domain.IGetAutoScrollUseCase
import org.kabiri.android.usbterminal.domain.IGetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.ISetAutoScrollUseCase
import org.kabiri.android.usbterminal.domain.ISetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.model.defaultBaudRate
import javax.inject.Inject

/**
 * Created by Ali Kabiri on 22.12.2023.
 */
@HiltViewModel
internal class SettingViewModel
    @Inject
    constructor(
        private val getBaudRate: IGetCustomBaudRateUseCase,
        private val setBaudRate: ISetCustomBaudRateUseCase,
        private val getAutoScroll: IGetAutoScrollUseCase,
        private val setAutoScroll: ISetAutoScrollUseCase,
    ) : ViewModel() {
        val currentBaudRate: Flow<Int>
            get() = getBaudRate()

        fun setNewBaudRate(baudRate: Int) = setBaudRate(baudRate)

        val currentAutoScroll: Flow<Boolean>
            get() = getAutoScroll()

        fun setAutoScrollEnabled(enabled: Boolean) = setAutoScroll(enabled)

        fun resetDefault() {
            setBaudRate(defaultBaudRate)
        }
    }
