package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.domain.IArduinoUseCase
import org.kabiri.android.usbterminal.domain.IUsbUseCase
import org.kabiri.android.usbterminal.util.IResourceProvider
import org.kabiri.android.usbterminal.util.isCloneArduinoBoard
import org.kabiri.android.usbterminal.util.isOfficialArduinoBoard

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainActivityViewModelTest {
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    val mockArduinoUsecase: IArduinoUseCase = mockk(relaxed = true)
    val mockUsbUseCase: IUsbUseCase = mockk(relaxed = true)
    val mockResourceProvider: IResourceProvider = mockk(relaxed = true)

    private lateinit var sut: MainActivityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sut =
            MainActivityViewModel(
                arduinoUseCase = mockArduinoUsecase,
                usbUseCase = mockUsbUseCase,
                resourceProvider = mockResourceProvider,
            )
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        unmockkStatic(UsbDevice::isOfficialArduinoBoard, UsbDevice::isCloneArduinoBoard)
        clearAllMocks()
    }

    @Test
    fun `startObservingUsbDevice updates infoMessage and calls openDeviceAndPort when device is emitted`() =
        runTest {
            // arrange
            val expected = 0x0043
            val deviceFlow = MutableStateFlow<UsbDevice?>(null)
            val mockDevice: UsbDevice = mockk(relaxed = true)
            every { mockDevice.vendorId } returns expected
            every { mockUsbUseCase.usbDevice } returns deviceFlow
            every { mockArduinoUsecase.openDeviceAndPort(mockDevice) } returns Unit

            // act
            sut.startObservingUsbDevice()
            deviceFlow.value = mockDevice
            advanceUntilIdle()

            // assert
            verify(exactly = 1) { mockArduinoUsecase.openDeviceAndPort(mockDevice) }
            assertThat(sut.infoMessage.value).contains(expected.toString())
        }

    @Test
    fun `connect emits expected message when device list is empty`() =
        runTest {
            // arrange
            val expected = "this is an error"
            every { mockUsbUseCase.scanForUsbDevices() } returns emptyList()
            every { mockResourceProvider.getString(R.string.helper_error_usb_devices_not_attached) } returns expected

            // act
            sut.connect()

            // assert
            assertThat(sut.errorMessage.value).isEqualTo(expected)
        }

    @Test
    fun `connect emits expected message and calls requestPermission anyways when the device is unknown`() =
        runTest {
            // arrange
            val expectedError = "device not found"
            val expectedInfo = "connecting anyways"

            val fakeDevice: UsbDevice = mockk(relaxed = true)
            mockkStatic(UsbDevice::isOfficialArduinoBoard, UsbDevice::isCloneArduinoBoard)
            every { fakeDevice.isOfficialArduinoBoard() } returns false
            every { fakeDevice.isCloneArduinoBoard() } returns false

            every { mockUsbUseCase.scanForUsbDevices() } returns listOf(fakeDevice)
            every { mockResourceProvider.getString(R.string.helper_error_arduino_device_not_found) } returns expectedError
            every { mockResourceProvider.getString(R.string.helper_error_connecting_anyway) } returns expectedInfo

            // act
            sut.connect()

            // assert
            assertThat(sut.errorMessage.value).isEqualTo(expectedError)
            assertThat(sut.infoMessage.value).isEqualTo(expectedInfo)
            verify(exactly = 1) { mockUsbUseCase.requestPermission(fakeDevice) }
        }

    @Test
    fun `connect emits expected message and calls requestPermission when the device is a clone`() =
        runTest {
            // arrange
            val expected = "connecting anyways"

            val fakeDevice: UsbDevice = mockk(relaxed = true)
            mockkStatic(UsbDevice::isOfficialArduinoBoard, UsbDevice::isCloneArduinoBoard)
            every { fakeDevice.isOfficialArduinoBoard() } returns false
            every { fakeDevice.isCloneArduinoBoard() } returns true

            every { mockUsbUseCase.scanForUsbDevices() } returns listOf(fakeDevice)
            every { mockResourceProvider.getString(R.string.helper_error_connecting_anyway) } returns expected

            // act
            sut.connect()

            // assert
            assertThat(sut.infoMessage.value).isEqualTo(expected)
            verify(exactly = 1) { mockUsbUseCase.requestPermission(fakeDevice) }
        }

    @Test
    fun `connect calls requestPermission when the device is official`() =
        runTest {
            // arrange
            val fakeDevice: UsbDevice = mockk(relaxed = true)
            mockkStatic(UsbDevice::isOfficialArduinoBoard, UsbDevice::isCloneArduinoBoard)
            every { fakeDevice.isOfficialArduinoBoard() } returns true
            every { fakeDevice.isCloneArduinoBoard() } returns false

            every { mockUsbUseCase.scanForUsbDevices() } returns listOf(fakeDevice)

            // act
            sut.connect()

            // assert
            verify(exactly = 1) { mockUsbUseCase.requestPermission(fakeDevice) }
            assertThat(sut.infoMessage.value).isEqualTo("")
            assertThat(sut.errorMessage.value).isEqualTo("")
            assertThat(sut.output.value).isEqualTo("")
        }

    @Test
    fun `connectIfAlreadyHasPermission does nothing when no usb device exists`() =
        runTest {
            // arrange
            every { mockUsbUseCase.usbDevice } returns MutableStateFlow(null)

            // act
            sut.connectIfAlreadyHasPermission()

            // assert
            verify(exactly = 0) { mockUsbUseCase.hasPermission(any()) }
            verify(exactly = 0) { mockArduinoUsecase.openDeviceAndPort(any()) }
        }

    @Test
    fun `connectIfAlreadyHasPermission calls hasPermission and openDeviceAndPort when usb device exists`() =
        runTest {
            // arrange
            val mockDevice: UsbDevice = mockk(relaxed = true)
            val fakeVendorId = 0x0043
            every { mockDevice.vendorId } returns fakeVendorId
            every { mockUsbUseCase.usbDevice } returns MutableStateFlow(mockDevice)
            every { mockUsbUseCase.hasPermission(mockDevice) } returns true
            every { mockArduinoUsecase.openDeviceAndPort(any()) } returns Unit

            // act
            sut.connectIfAlreadyHasPermission()
            advanceUntilIdle()

            // assert
            verify { mockUsbUseCase.hasPermission(mockDevice) }
            verify { mockArduinoUsecase.openDeviceAndPort(mockDevice) }
        }
}
