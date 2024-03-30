package org.kabiri.android.usbterminal.ui.setting

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme

@Composable
fun SettingsHeader() {

    Column(modifier = Modifier.padding(16.dp)) {

        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )

        Text(
            text = stringResource(id = R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun SettingsHeaderPreviewNight() {
    UsbTerminalTheme {
        SettingsHeader()
    }
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
fun SettingsHeaderPreviewDay() {
    UsbTerminalTheme {
        SettingsHeader()
    }
}