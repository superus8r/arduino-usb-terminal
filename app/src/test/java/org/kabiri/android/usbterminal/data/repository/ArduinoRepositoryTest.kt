package org.kabiri.android.usbterminal.data.repository

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.domain.IGetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.model.DEFAULT_BAUD_RATE

@OptIn(ExperimentalCoroutinesApi::class)
internal class ArduinoRepositoryTest {
    private val mockContext: Context = mockk(relaxed = true)
    private val mockUsbManager: UsbManager = mockk(relaxed = true)
    private val mockConnection: UsbDeviceConnection = mockk(relaxed = true)
    private val mockSerial: UsbSerialDevice = mockk(relaxed = true)
    private val mockDevice: UsbDevice = mockk(relaxed = true)
    private val receiver = ArduinoSerialReceiver()
    private val mockGetBaud: IGetCustomBaudRateUseCase = mockk(relaxed = true)

    private lateinit var sut: ArduinoRepository

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 1

        every { mockContext.getSystemService(Context.USB_SERVICE) } returns mockUsbManager
        every { mockGetBaud.invoke() } returns emptyFlow()
        sut =
            ArduinoRepository(
                context = mockContext,
                arduinoSerialReceiver = receiver,
                getBaudRate = mockGetBaud,
            )
    }

    @After
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `openDeviceAndPort opens serial and emits connection opened`() =
        runTest {
            // arrange
            val expected = "opened"
            mockkStatic(UsbSerialDevice::class)
            every { mockUsbManager.openDevice(mockDevice) } returns mockConnection
            every {
                UsbSerialDevice.createUsbSerialDevice(
                    mockDevice,
                    mockConnection,
                )
            } returns mockSerial
            every { mockSerial.open() } returns true
            every { mockContext.getString(R.string.helper_info_serial_connection_opened) } returns expected

            // act
            sut.openDeviceAndPort(mockDevice)
            advanceUntilIdle()

            // assert
            verify { mockUsbManager.openDevice(mockDevice) }
            verify { mockSerial.open() }
            verify { mockSerial.setBaudRate(DEFAULT_BAUD_RATE) }
            verify { mockSerial.read(receiver) }
            assertThat(sut.infoMessageFlow.first()).isEqualTo(expected)
        }

    @Test
    fun `openDeviceAndPort emits error when serial port fails to open`() =
        runTest {
            // arrange
            val expected = "not opened"
            mockkStatic(UsbSerialDevice::class)
            every { mockUsbManager.openDevice(mockDevice) } returns mockConnection
            every {
                UsbSerialDevice.createUsbSerialDevice(
                    mockDevice,
                    mockConnection,
                )
            } returns mockSerial
            every { mockSerial.open() } returns false
            every { mockContext.getString(R.string.helper_error_serial_connection_not_opened) } returns expected

            // act
            sut.openDeviceAndPort(mockDevice)
            advanceUntilIdle()

            // assert
            assertThat(sut.errorMessageFlow.first()).isEqualTo(expected)
        }

    @Test
    fun `openDeviceAndPort emits info and closes when serial device is null`() =
        runTest {
            // arrange
            val expected = "port null"
            mockkStatic(UsbSerialDevice::class)
            every { mockUsbManager.openDevice(mockDevice) } returns mockConnection
            every { UsbSerialDevice.createUsbSerialDevice(mockDevice, mockConnection) } returns null
            every { mockContext.getString(R.string.helper_error_serial_port_is_null) } returns expected

            // act
            sut.openDeviceAndPort(mockDevice)
            advanceUntilIdle()

            // assert
            verify { mockConnection.close() }
            assertThat(sut.infoMessageFlow.first()).isEqualTo(expected)
        }

    @Test
    fun `serialWrite writes bytes when initialized`() =
        runTest {
            // arrange
            val expected = "fakeCommand"
            mockkStatic(UsbSerialDevice::class)
            every { mockUsbManager.openDevice(mockDevice) } returns mockConnection
            every {
                UsbSerialDevice.createUsbSerialDevice(
                    mockDevice,
                    mockConnection,
                )
            } returns mockSerial
            every { mockSerial.open() } returns true
            every { mockContext.getString(any()) } returns "" // ignore other strings
            sut.openDeviceAndPort(mockDevice)
            advanceUntilIdle()

            // act
            val actual = sut.serialWrite(expected)

            // assert
            assertThat(actual).isTrue()
            verify { mockSerial.write(expected.toByteArray()) }
        }

    @Test
    fun `serialWrite emits error when not initialized`() =
        runTest {
            // arrange
            val expected = "port null"
            every { mockContext.getString(R.string.helper_error_serial_port_is_null) } returns expected

            // act
            val actual = sut.serialWrite("doesn't matter")

            // assert
            assertThat(actual).isFalse()
            assertThat(sut.errorMessageFlow.first()).isEqualTo(expected)
        }

    @Test
    fun `disconnect closes connection and emits closed info`() =
        runTest {
            // arrange
            val expected = "closed"
            val unexpected = "opened"
            mockkStatic(UsbSerialDevice::class)
            every { mockUsbManager.openDevice(mockDevice) } returns mockConnection
            every {
                UsbSerialDevice.createUsbSerialDevice(
                    mockDevice,
                    mockConnection,
                )
            } returns mockSerial
            every { mockSerial.open() } returns true
            every { mockContext.getString(R.string.helper_info_serial_connection_opened) } returns unexpected
            every { mockContext.getString(R.string.helper_info_serial_connection_closed) } returns expected
            sut.openDeviceAndPort(mockDevice)
            advanceUntilIdle()

            // act
            sut.disconnect()
            advanceUntilIdle()

            // assert
            verify { mockConnection.close() }
            assertThat(sut.messageFlow.first()).isEqualTo(expected)
        }
}
