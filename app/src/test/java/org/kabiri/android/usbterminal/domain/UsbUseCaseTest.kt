package org.kabiri.android.usbterminal.domain

import android.hardware.usb.UsbDevice
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.data.repository.IUsbRepository

internal class UsbUseCaseTest {

    private val mockRepo = mockk<IUsbRepository>(relaxed = true)
    private lateinit var sut: UsbUseCase

    @Before
    fun setUp() {
        sut = UsbUseCase(mockRepo)
    }

    @After
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `usbDevice delegates to repository`() {
        // Arrange
        val expected = MutableSharedFlow<UsbDevice?>()
        every { mockRepo.usbDevice } returns expected
        sut = UsbUseCase(mockRepo) // Reinitialize with new mock repo

        // Act
        val actual = sut.usbDevice

        assertThat(actual).isSameInstanceAs(expected)
        verify { mockRepo.usbDevice }
    }

    @Test
    fun `infoMessageFlow delegates to repository`() {
        // Arrange
        val expected = MutableSharedFlow<String>()
        every { mockRepo.infoMessageFlow } returns expected
        sut = UsbUseCase(mockRepo) // Reinitialize with new mock repo

        // Act
        val actual = sut.infoMessageFlow

        // Assert
        assertThat(actual).isSameInstanceAs(expected)
        verify { mockRepo.infoMessageFlow }
    }

    @Test
    fun `scanForUsbDevices delegates to repository`() {
        // Arrange
        val device1 = mockk<UsbDevice>()
        val device2 = mockk<UsbDevice>()
        val expected = listOf(device1, device2)
        every { mockRepo.scanForArduinoDevices() } returns expected

        // Act
        val actual = sut.scanForUsbDevices()

        // Assert
        assertThat(actual).containsExactlyElementsIn(expected)
        verify { mockRepo.scanForArduinoDevices() }
    }

    @Test
    fun `hasPermission delegates to repository`() {
        // Arrange
        val device = mockk<UsbDevice>()
        every { mockRepo.hasPermission(device) } returns true

        // Act
        val result = sut.hasPermission(device)

        // Assert
        assertThat(result).isTrue()
        verify { mockRepo.hasPermission(device) }
    }

    @Test
    fun `requestPermission delegates to repository`() {
        // Arrange
        val device = mockk<UsbDevice>()

        // Act
        sut.requestPermission(device)

        // Assert
        verify { mockRepo.requestUsbPermission(device) }
    }

    @Test
    fun `disconnect delegates to repository`() {
        // Arrange
        // Act
        sut.disconnect()

        // Assert
        verify { mockRepo.disconnect() }
    }
}