package org.kabiri.android.usbterminal.domain

import android.hardware.usb.UsbDevice
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.kabiri.android.usbterminal.data.repository.IArduinoRepository

internal class ArduinoUseCaseTest {

    private val mockRepo = mockk<IArduinoRepository>(relaxed = true)
    private val sut = ArduinoUseCase(mockRepo)

    @Test
    fun `openDeviceAndPort delegates to repository`() {
        // Arrange
        val device = mockk<UsbDevice>()

        // Act
        sut.openDeviceAndPort(device)

        // Assert
        verify { mockRepo.openDeviceAndPort(device) }
    }

    @Test
    fun `disconnect delegates to repository`() {
        // Arrange
        // Act
        sut.disconnect()

        // Assert
        verify { mockRepo.disconnect() }
    }

    @Test
    fun `serialWrite returns repository result`() {
        // Arrange
        val fakeCommand = "doesn't matter"
        every { mockRepo.serialWrite(fakeCommand) } returns true

        // Act
        val result = sut.serialWrite(fakeCommand)

        // Assert
        assertThat(result).isTrue()
        verify { mockRepo.serialWrite(fakeCommand) }
    }

    @Test
    fun `messageFlow emits repository messages`() = runTest {
        // Arrange
        val expected = listOf("m1", "m2")
        every { mockRepo.messageFlow } returns flowOf("m1", "m2")

        // Act
        val actual = sut.messageFlow.toList()

        // Assert
        assertThat(actual).containsExactlyElementsIn(expected)
    }

    @Test
    fun `infoMessageFlow emits repository infos`() = runTest {
        // Arrange
        val expected = listOf("i1", "i2")
        every { mockRepo.infoMessageFlow } returns flowOf("i1", "i2")

        // Act
        val actual = sut.infoMessageFlow.toList()

        // Assert
        assertThat(actual).containsExactlyElementsIn(expected)
    }

    @Test
    fun `errorMessageFlow emits repository errors`() = runTest {
        // Arrange
        val expected = listOf("e1", "e2")
        every { mockRepo.errorMessageFlow } returns flowOf("e1", "e2")

        // Act
        val actual = sut.errorMessageFlow.toList()

        // Assert
        assertThat(actual).containsExactlyElementsIn(expected)
    }
}