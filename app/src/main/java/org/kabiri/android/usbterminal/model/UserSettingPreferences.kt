package org.kabiri.android.usbterminal.model

internal const val DEFAULT_BAUD_RATE: Int = 9600
internal const val DEFAULT_AUTO_SCROLL: Boolean = true

data class UserSettingPreferences(
    val baudRate: Int = DEFAULT_BAUD_RATE, // Arduino default
    val autoScroll: Boolean = DEFAULT_AUTO_SCROLL,
)
