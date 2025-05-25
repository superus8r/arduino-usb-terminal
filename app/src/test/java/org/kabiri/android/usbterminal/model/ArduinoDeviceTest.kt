package org.kabiri.android.usbterminal.model

import android.hardware.usb.UsbDevice
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.model.ArduinoDevice.ArduinoType
import org.kabiri.android.usbterminal.util.isCloneArduinoBoard
import org.kabiri.android.usbterminal.util.isOfficialArduinoBoard

class ArduinoDeviceTest {

    @Before
    fun setUp() {
        mockkStatic(
            UsbDevice::isOfficialArduinoBoard,
            UsbDevice::isCloneArduinoBoard
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(
            UsbDevice::isOfficialArduinoBoard,
            UsbDevice::isCloneArduinoBoard
        )
    }

    @Test
    fun `type is OFFICIAL when isOfficialArduinoBoard returns true`() {
        // Arrange
        val device = mockk<UsbDevice>()
        every { device.isOfficialArduinoBoard() } returns true
        every { device.isCloneArduinoBoard() } returns false

        // Act
        val sut = ArduinoDevice(device)

        // Assert
        assertThat(sut.type).isEqualTo(ArduinoType.OFFICIAL)
    }

    @Test
    fun `type is CLONE when isCloneArduinoBoard returns true`() {
        // Arrange
        val device = mockk<UsbDevice>()
        every { device.isOfficialArduinoBoard() } returns false
        every { device.isCloneArduinoBoard() } returns true

        // Act
        val sut = ArduinoDevice(device)

        // Assert
        assertThat(sut.type).isEqualTo(ArduinoType.CLONE)
    }

    @Test
    fun `type is UNKNOWN when both extensions return false`() {
        // Arrange
        val device = mockk<UsbDevice>()
        every { device.isOfficialArduinoBoard() } returns false
        every { device.isCloneArduinoBoard() } returns false

        // Act
        val sut = ArduinoDevice(device)

        // Assert
        assertThat(sut.type).isEqualTo(ArduinoType.UNKNOWN)
    }
}