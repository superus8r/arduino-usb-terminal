package org.kabiri.android.usbterminal.model

internal const val defaultBaudRate: Int = 9600

data class UserSettingPreferences(
    val baudRate: Int = defaultBaudRate, // Arduino default
    val autoScroll: Boolean = true,
)
