package org.kabiri.android.usbterminal.viewmodel

import android.hardware.usb.UsbDevice
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
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
import org.kabiri.android.usbterminal.model.OutputText
import org.kabiri.android.usbterminal.util.IResourceProvider
import org.kabiri.android.usbterminal.util.isCloneArduinoBoard
import org.kabiri.android.usbterminal.util.isOfficialArduinoBoard

private const val OFFICIAL_VENDOR_ID = 0x2341
private const val OFFICIAL_PRODUCT_ID = 0x0043

private const val CLONE_VENDOR_ID = 0x1A86
private const val CLONE_PRODUCT_ID = 0x7523

private const val OTHER_VENDOR_ID = 0x1234 // random id
private const val OTHER_PRODUCT_ID = 0x1234 // random id

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
            val expected = OFFICIAL_VENDOR_ID
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
    fun `startObservingUsbDevice does not call openDeviceAndPort when device is null`() =
        runTest {
            // arrange
            val expected = null
            val deviceFlow = MutableStateFlow<UsbDevice?>(expected)
            every { mockUsbUseCase.usbDevice } returns deviceFlow

            // act
            sut.startObservingUsbDevice()
            deviceFlow.value = expected
            advanceUntilIdle()

            // assert
            verify(exactly = 0) { mockArduinoUsecase.openDeviceAndPort(any()) }
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
            every { fakeDevice.vendorId } returns OTHER_VENDOR_ID
            every { fakeDevice.productId } returns OTHER_PRODUCT_ID

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
            every { fakeDevice.vendorId } returns CLONE_VENDOR_ID
            every { fakeDevice.productId } returns CLONE_PRODUCT_ID

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
            every { fakeDevice.vendorId } returns OFFICIAL_VENDOR_ID
            every { fakeDevice.productId } returns OFFICIAL_PRODUCT_ID

            every { mockUsbUseCase.scanForUsbDevices() } returns listOf(fakeDevice)

            // act
            sut.connect()

            // assert
            verify(exactly = 1) { mockUsbUseCase.requestPermission(fakeDevice) }
            assertThat(sut.infoMessage.value).isEqualTo("")
            assertThat(sut.errorMessage.value).isEqualTo("")
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
            val fakeVendorId = OFFICIAL_VENDOR_ID
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

    @Test
    fun `disconnect calls disconnect on both usbUseCase and arduinoUseCase`() =
        runTest {
            // arrange
            // act
            sut.disconnect()

            // assert
            verify(exactly = 1) { mockUsbUseCase.disconnect() }
            verify(exactly = 1) { mockArduinoUsecase.disconnect() }
        }

    @Test
    fun `serialWrite updates output and returns result from arduinoUseCase`() =
        runTest {
            // arrange
            val expected = "TEST_COMMAND"
            every { mockArduinoUsecase.serialWrite(expected) } returns true

            // act
            val result = sut.serialWrite(expected)

            // assert
            verify { mockArduinoUsecase.serialWrite(expected) }
            assertThat(result).isTrue()
            val outputText = sut.output2.last().text
            assertThat(outputText).contains(expected)
        }

    @Test
    fun `startObservingTerminalOutput appends arduinoInfo when only arduinoInfo is not empty`() =
        runTest {
            // arrange
            val arduinoDefaultFlow = MutableStateFlow("")
            val arduinoInfoFlow = MutableStateFlow("arduino info message")
            val arduinoErrorFlow = MutableStateFlow("")
            val usbInfoFlow = MutableStateFlow("")

            every { mockArduinoUsecase.messageFlow } returns arduinoDefaultFlow
            every { mockArduinoUsecase.infoMessageFlow } returns arduinoInfoFlow
            every { mockArduinoUsecase.errorMessageFlow } returns arduinoErrorFlow
            every { mockUsbUseCase.infoMessageFlow } returns usbInfoFlow
            every { mockResourceProvider.getString(any()) } returns ""

            // act
            sut.startObservingTerminalOutput()
            advanceUntilIdle()

            // assert
            assertThat(sut.output2).isNotEmpty()
            val last = sut.output2.last()
            assertThat(last.text).isEqualTo("arduino info message")
            assertThat(last.type).isEqualTo(OutputText.OutputType.TYPE_INFO)
        }

    @Test
    fun `startObservingTerminalOutput appends arduinoDefault when all outputs are empty`() =
        runTest {
            // arrange
            val arduinoDefaultFlow = MutableStateFlow("default message")
            val arduinoInfoFlow = MutableStateFlow("")
            val arduinoErrorFlow = MutableStateFlow("")
            val usbInfoFlow = MutableStateFlow("")

            every { mockArduinoUsecase.messageFlow } returns arduinoDefaultFlow
            every { mockArduinoUsecase.infoMessageFlow } returns arduinoInfoFlow
            every { mockArduinoUsecase.errorMessageFlow } returns arduinoErrorFlow
            every { mockUsbUseCase.infoMessageFlow } returns usbInfoFlow
            every { mockResourceProvider.getString(any()) } returns ""

            // act
            sut.startObservingTerminalOutput()
            advanceUntilIdle()

            // assert
            assertThat(sut.output2).isNotEmpty()
            val last = sut.output2.last()
            assertThat(last.text).isEqualTo("default message")
            assertThat(last.type).isEqualTo(OutputText.OutputType.TYPE_NORMAL)
        }
}
