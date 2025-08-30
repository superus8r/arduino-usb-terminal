package org.kabiri.android.usbterminal.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.kabiri.android.usbterminal.model.defaultBaudRate

@Composable
internal fun SettingContent(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel,
    onDismiss: () -> Unit,
) {
    val currentBaudRate by settingViewModel.currentBaudRate.collectAsState(defaultBaudRate)
    val autoScrollEnabled by settingViewModel.currentAutoScroll.collectAsState(true)

    Column(modifier = modifier) {
        // Settings Header
        SettingsHeader()

        // Auto-scroll Setting
        SettingAutoScrollItem(
            enabled = autoScrollEnabled,
            onToggle = settingViewModel::setAutoScrollEnabled,
        )

        // Baud Rate Setting
        SettingValueItem(
            currentValue = currentBaudRate,
            onNewValue = settingViewModel::setNewBaudRate,
        )

        // Reset Default Button
        SettingResetOrDismissSheetButtonRow(
            onClickReset = {
                settingViewModel.resetDefault()
                onDismiss()
            },
            onClickDismiss = onDismiss,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
