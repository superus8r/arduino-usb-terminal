package org.kabiri.android.usbterminal.ui.setting

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.kabiri.android.usbterminal.R

@Composable
internal fun SettingResetDefaultButton(
    onClick: () -> Unit
) {
    Button(onClick = onClick) {
        Text(text = stringResource(id = R.string.settings_bt_reset_default))
    }
}

@Preview(showBackground = true)
@Composable
fun SettingResetDefaultButtonPreview() {
    SettingResetDefaultButton(onClick = {})
}