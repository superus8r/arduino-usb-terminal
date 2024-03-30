package org.kabiri.android.usbterminal.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.kabiri.android.usbterminal.model.defaultBaudRate

@Composable
internal fun SettingContent(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel
) {
    val currentBaudRate by settingViewModel.currentBaudRate.collectAsState(defaultBaudRate)

    Column(modifier = modifier) {

        // Baud Rate Setting
        SettingValueItem(
            currentValue = currentBaudRate,
            onNewValue = settingViewModel::setNewBaudRate
        )
    }
}

