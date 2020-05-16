package org.kabiri.android.usbterminal.data

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Created by Ali Kabiri on 16.04.20.
 */
class SettingsReaderTest {

    private val mockContext = mockk<Context>()

    private lateinit var sut: SettingsReader
    private lateinit var spyPrefs: SharedPreferences

    @Before
    fun setup() {
        spyPrefs = spyk {
            every { getBoolean(any(), false) } returns true
        }
        sut = SettingsReader(mockContext, spyPrefs)
    }

    @Test
    fun preferenceChangeListenerGetsRegistered() {
        verify { spyPrefs.registerOnSharedPreferenceChangeListener(any()) }
    }
}