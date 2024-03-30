package org.kabiri.android.usbterminal.ui.setting

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme

@Composable
internal fun SettingValueItem(
    currentValue: Int,
    onNewValue: (Int) -> Unit
) {
    var inputValue by remember { mutableStateOf(currentValue.toString()) }
    var showSaveIcon by remember { mutableStateOf(false) }

    // keep baudRateInput updated with currentBaudRate
    LaunchedEffect(currentValue) {
        inputValue = currentValue.toString()
    }

    OutlinedTextField(
        value = inputValue,
        onValueChange = { value: String ->
            inputValue = value
            showSaveIcon = value.toIntOrNull() != currentValue
        },
        label = { Text("Baud Rate") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        trailingIcon = {
            if (showSaveIcon) {
                IconButton(onClick = {
                    inputValue.toIntOrNull()?.let { baudRate ->
                        onNewValue(baudRate)
                        showSaveIcon = false
                    }
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Save"
                    )
                }
            }
        },
        isError = inputValue.toIntOrNull() == null
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