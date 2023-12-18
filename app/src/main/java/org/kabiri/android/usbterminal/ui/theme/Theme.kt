package org.kabiri.android.usbterminal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val darkColorTheme = darkColorScheme(
    primary = Color.Black,
)

private val lightColorTheme = lightColorScheme(
)

private var isLightCustom: Boolean = false

@Composable
fun UsbTerminalTheme(darkTheme: Boolean? = null, content: @Composable () -> Unit) {
    isLightCustom = if (darkTheme == null) !isSystemInDarkTheme() else !darkTheme

    val colorScheme = if (isLightCustom) {
        lightColorTheme
    } else {
        darkColorTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}