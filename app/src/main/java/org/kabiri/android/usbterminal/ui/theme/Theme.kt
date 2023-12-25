package org.kabiri.android.usbterminal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val darkColorTheme = darkColorScheme(
    primary = ColorPrimaryNight,
    onPrimary = ColorPrimaryTextNight,
    secondary = ColorAccentNight,
    onSecondary = ColorSecondaryTextNight,
    background = ColorBackgroundNight,
)

private val lightColorTheme = lightColorScheme(
    primary = ColorPrimaryDay,
    onPrimary = ColorPrimaryTextDay,
    secondary = ColorAccentDay,
    onSecondary = ColorSecondaryTextDay,
    background = ColorBackgroundDay,
)

private var isLightCustom: Boolean = true

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