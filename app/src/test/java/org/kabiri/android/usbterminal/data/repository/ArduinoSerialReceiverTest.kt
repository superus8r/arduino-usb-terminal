package org.kabiri.android.usbterminal.data.repository

import android.util.Log
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.UnsupportedEncodingException

internal class ArduinoSerialReceiverTest {
    private lateinit var sut: ArduinoSerialReceiver

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        sut = ArduinoSerialReceiver()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `onReceivedData emits error on UnsupportedEncodingException`() =
        runTest {
            // arrange
            val exceptionMessage = "Unsupported Encoding"
            every { Log.i(any(), any()) } throws UnsupportedEncodingException(exceptionMessage)

            // act
            sut.onReceivedData(byteArrayOf(1, 2, 3))

            // assert
            assertThat(sut.liveErrorOutput.value).isEqualTo(exceptionMessage)
        }

    @Test
    fun `onReceivedData emits error on generic Exception`() =
        runTest {
            // arrange
            val exceptionMessage = "Generic Exception"
            every { Log.i(any(), any()) } throws RuntimeException(exceptionMessage)

            // act
            sut.onReceivedData(byteArrayOf(1, 2, 3))

            // assert
            assertThat(sut.liveErrorOutput.value).isEqualTo(exceptionMessage)
        }
}
