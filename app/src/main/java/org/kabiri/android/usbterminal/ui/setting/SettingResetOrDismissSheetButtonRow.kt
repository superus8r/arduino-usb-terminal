package org.kabiri.android.usbterminal.ui.setting

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme

@Composable
internal fun SettingResetOrDismissSheetButtonRow(
    onClickReset: () -> Unit,
    onClickDismiss: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Row {

            OutlinedButton(onClick = onClickReset) {
                Text(text = stringResource(id = R.string.settings_bt_reset_default))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = onClickDismiss) {
                Text(text = stringResource(id = R.string.settings_bt_dismiss_sheet))
            }
        }

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = stringResource(id = R.string.settings_bottom_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun SettingResetDefaultButtonPreviewNight() {
    UsbTerminalTheme {
        SettingResetOrDismissSheetButtonRow(
            onClickReset = {},
            onClickDismiss = {},
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun SettingResetDefaultButtonPreviewDay() {
    UsbTerminalTheme {
        SettingResetOrDismissSheetButtonRow(
            onClickReset = {},
            onClickDismiss = {},
        )
    }
}