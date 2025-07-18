package org.kabiri.android.usbterminal.data.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.Constants

@OptIn(ExperimentalCoroutinesApi::class)
internal class UsbRepositoryTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val mockContext: Context = mockk(relaxed = true)
    private val mockUsbManager: UsbManager = mockk(relaxed = true)
    val mockUsbDevice1: UsbDevice = mockk()
    val mockUsbDevice2: UsbDevice = mockk()

    private lateinit var sut: UsbRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { mockUsbManager.deviceList } returns hashMapOf(
            "device1" to mockUsbDevice1,
            "device2" to mockUsbDevice2,
        )
        every { mockContext.getSystemService(UsbManager::class.java) } returns mockUsbManager

        sut = UsbRepository(
            context = mockContext,
            scope = testScope,
        )
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `scanForArduinoDevices returns list of usb devices from usbManager`() = runTest {
        // arrange
        val expectedDeviceList = listOf(mockUsbDevice1, mockUsbDevice2)

        // act
        val actualDeviceList = sut.scanForArduinoDevices()
        advanceUntilIdle()

        // assert
        verify { mockUsbManager.deviceList }
        assertThat(actualDeviceList).isNotEmpty()
        assertThat(actualDeviceList).hasSize(actualDeviceList.size)
        assertThat(actualDeviceList.containsAll(expectedDeviceList)).isTrue()
    }

    @Test
    fun `requestUsbPermission registers receiver and requests permission`() = runTest {
        // arrange
        mockkStatic(PendingIntent::class)
        mockkStatic(ContextCompat::class)
        val mockIntent: PendingIntent = mockk()
        every {
            PendingIntent.getBroadcast(
                mockContext,
                0,
                any(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } returns mockIntent
        every {
            ContextCompat.registerReceiver(
                mockContext,
                any<UsbPermissionReceiver>(),
                IntentFilter(Constants.ACTION_USB_PERMISSION),
                ContextCompat.RECEIVER_EXPORTED
            )
        } returns null

        // act
        sut.requestUsbPermission(mockUsbDevice1)
        advanceUntilIdle()

        // assert
        assertThat(sut.usbDevice.first()).isEqualTo(mockUsbDevice1)
        verify {
            PendingIntent.getBroadcast(
                mockContext,
                0,
                any<Intent>(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            ContextCompat.registerReceiver(
                mockContext,
                any<UsbPermissionReceiver>(),
                any<IntentFilter>(),
                ContextCompat.RECEIVER_EXPORTED
            )
            mockUsbManager.requestPermission(mockUsbDevice1, mockIntent)
        }
    }

    @Test
    fun `hasPermission returns true when granted`() = runTest {
        // arrange
        every { mockUsbManager.hasPermission(mockUsbDevice1) } returns true

        // act
        val actual = sut.hasPermission(mockUsbDevice1)

        // assert
        assertThat(actual).isTrue()
        verify { mockUsbManager.hasPermission(mockUsbDevice1) }
    }

    @Test
    fun `hasPermission returns false when denied`() = runTest {
        // arrange
        every { mockUsbManager.hasPermission(mockUsbDevice2) } returns false

        // act
        val actual = sut.hasPermission(mockUsbDevice2)

        // assert
        assertThat(actual).isFalse()
        verify { mockUsbManager.hasPermission(mockUsbDevice2) }
    }

    @Test
    fun `onPermissionResult emits info when granted`() = runTest(testDispatcher) {
        // arrange
        val fakeGranted = true
        val fakeId = 123
        val fakeString = "doesn't matter"
        val fakeMsg = "permission granted"
        val fakeDeviceInfo = "$fakeString $fakeString $fakeId $fakeId"
        val expected = "\n$fakeMsg $fakeDeviceInfo"
        every { mockContext.getString(any()) } returns fakeMsg
        every { mockUsbDevice1.manufacturerName } returns fakeString
        every { mockUsbDevice1.productName } returns fakeString
        every { mockUsbDevice1.vendorId } returns fakeId
        every { mockUsbDevice1.productId } returns fakeId

        // act
        sut.onPermissionResult(
            device = mockUsbDevice1,
            granted = fakeGranted
        )
        advanceUntilIdle()

        // assert
        assertThat(sut.infoMessageFlow.first()).isEqualTo(expected)
    }

    @Test
    fun `onPermissionResult emits info when denied`() = runTest(testDispatcher) {
        // arrange
        val fakeGranted = false
        val fakeId = 123
        val fakeString = "doesn't matter"
        val fakeMsg = "permission denied"
        val fakeDeviceInfo = "$fakeString $fakeString $fakeId $fakeId"
        val expected = "$fakeMsg $fakeDeviceInfo"
        every { mockContext.getString(any()) } returns fakeMsg
        every { mockUsbDevice1.manufacturerName } returns fakeString
        every { mockUsbDevice1.productName } returns fakeString
        every { mockUsbDevice1.vendorId } returns fakeId
        every { mockUsbDevice1.productId } returns fakeId

        // act
        sut.onPermissionResult(
            device = mockUsbDevice1,
            granted = fakeGranted
        )
        advanceUntilIdle()

        // assert
        assertThat(sut.infoMessageFlow.first()).isEqualTo(expected)
    }

    @Test
    fun `onDeviceAttached emits info when granted`() = runTest(testDispatcher) {
        // arrange
        val expected = "fake device attached"
        every { mockContext.getString(any()) } returns expected

        // act
        sut.onDeviceAttached(device = mockUsbDevice1)
        advanceUntilIdle()

        // assert
        assertThat(sut.infoMessageFlow.first()).isEqualTo(expected)
    }

    @Test
    fun `onDeviceDetached emits info when granted`() = runTest(testDispatcher) {
        // arrange
        val expected = "fake device detached"
        every { mockContext.getString(any()) } returns expected

        // act
        sut.onDeviceDetached(device = mockUsbDevice1)
        advanceUntilIdle()

        // assert
        assertThat(sut.infoMessageFlow.first()).isEqualTo(expected)
    }

    @Test
    fun `onUnknownAction emits info when granted`() = runTest(testDispatcher) {
        // arrange
        val expected = "strange action received"
        val mockIntent: Intent = mockk()
        every { mockContext.getString(any()) } returns expected

        // act
        sut.onUnknownAction(mockIntent)
        advanceUntilIdle()

        // assert
        assertThat(sut.infoMessageFlow.first()).isEqualTo(expected)
    }

    @Test
    fun `onDisconnect clears usbDevice`() = runTest {
        // arrange
        mockkStatic(PendingIntent::class)
        mockkStatic(ContextCompat::class)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockk()
        sut.requestUsbPermission(mockUsbDevice1)
        advanceUntilIdle()
        assertThat(sut.usbDevice.first()).isNotNull()

        // act
        sut.disconnect()
        advanceUntilIdle()

        // assert
        assertThat(sut.usbDevice.first()).isNull()
    }
}