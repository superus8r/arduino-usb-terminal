package org.kabiri.android.usbterminal.data

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ServiceNameHelperTest {

    private val mockContext = mockk<Context>()
    private val sut = ServiceNameHelper(mockContext)

    private lateinit var spyEditor: SharedPreferences.Editor
    private lateinit var spyPrefs: SharedPreferences

    @Before
    fun setup() {
        spyEditor = spyk()
        spyPrefs = spyk {
            every { edit() } returns spyEditor
        }
        sut.tPrefs = spyPrefs
    }

    @Test
    fun requestingServiceNameCallsTheSharedPrefsMethod() {

        sut.serviceName // this will call the property's get() method.
        verify { spyPrefs.edit() }
        verify { spyEditor.putString(any(), any()) }
        verify { spyEditor.apply() }
    }

}