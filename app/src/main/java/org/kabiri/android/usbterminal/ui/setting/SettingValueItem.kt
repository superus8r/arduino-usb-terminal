package org.kabiri.android.usbterminal.ui.setting

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme
import org.kabiri.android.usbterminal.ui.setting.SaveState.DEFAULT
import org.kabiri.android.usbterminal.ui.setting.SaveState.UNSAVED
import org.kabiri.android.usbterminal.ui.setting.SaveState.SAVED
import org.kabiri.android.usbterminal.ui.setting.SaveState.ERROR

/**
 * Enum class to represent the save state of the setting value.
 * - DEFAULT: the default state of the setting value when it has not been changed
 * - UNSAVED: the setting value has been changed but not saved
 * - SAVED: the setting value has been saved
 * - ERROR: the setting value is invalid
 */
private enum class SaveState {
    DEFAULT, UNSAVED, SAVED, ERROR,
}

/**
 * A composable that represents a setting item with a value that can be changed.
 *
 * @param currentValue the current value of the setting
 * @param onNewValue a callback that will be called when a new value is set
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SettingValueItem(
    currentValue: Int,
    onNewValue: (Int) -> Unit
) {
    var inputValue by remember { mutableStateOf(currentValue.toString()) }
    var saveState by remember { mutableStateOf(DEFAULT) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun onClickSaveNewValue() {
        inputValue.toIntOrNull()?.let { baudRate ->
            onNewValue(baudRate)
            saveState = SAVED
            keyboardController?.hide()
        }
    }

    // keep baudRateInput updated with currentBaudRate
    LaunchedEffect(currentValue) {
        inputValue = currentValue.toString()
    }

    OutlinedTextField(
        value = inputValue,
        onValueChange = { value: String ->
            val input = value.toIntOrNull()
            if (input == null) {
                saveState = ERROR
                return@OutlinedTextField
            }
            saveState = if (value.toIntOrNull() != currentValue) UNSAVED else DEFAULT
            inputValue = value
        },
        label = { Text(stringResource(id = R.string.settings_label_baud_rate)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        trailingIcon = {
            when (saveState) {
                DEFAULT -> {}
                UNSAVED -> {
                    IconButton(onClick = {
                        onClickSaveNewValue()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(id = R.string.settings_bt_save)
                        )
                    }
                }
                SAVED -> {
                    Text(
                        text = stringResource(id = R.string.settings_label_saved),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                ERROR -> {
                    Text(
                        text = stringResource(id = R.string.settings_label_error),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        },
        isError = saveState == ERROR,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions(
            onDone = {
                onClickSaveNewValue()
            }
        ),
    )
}

@Preview(showBackground = true)
@Composable
fun SettingValueItemPreview() {
    UsbTerminalTheme {
        SettingValueItem(
            currentValue = 9600,
            onNewValue = {}
        )
    }
}