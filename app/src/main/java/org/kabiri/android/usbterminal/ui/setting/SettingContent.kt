package org.kabiri.android.usbterminal.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.model.defaultBaudRate

@Composable
internal fun SettingContent(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel
) {
    val scope = rememberCoroutineScope()
    val currentBaudRate by settingViewModel.currentBaudRate.collectAsState(defaultBaudRate)
    var baudRateInput by remember { mutableStateOf(currentBaudRate.toString()) }
    var showSaveIcon by remember { mutableStateOf(false) }

    // keep baudRateInput updated with currentBaudRate
    LaunchedEffect(currentBaudRate) {
        baudRateInput = currentBaudRate.toString()
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = baudRateInput,
            onValueChange = { value: String ->
                baudRateInput = value
                showSaveIcon = value.toIntOrNull() != currentBaudRate
            },
            label = { Text("Baud Rate") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            trailingIcon = {
                if (showSaveIcon) {
                    IconButton(onClick = {
                        scope.launch {
                            baudRateInput.toIntOrNull()?.let { baudRate ->
                                settingViewModel.setNewBaudRate(baudRate)
                                showSaveIcon = false
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            },
            isError = baudRateInput.toIntOrNull() == null
        )
    }
}
