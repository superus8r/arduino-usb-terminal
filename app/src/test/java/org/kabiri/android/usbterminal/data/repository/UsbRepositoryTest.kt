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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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

        sut = UsbRepository(mockContext)
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
        assertThat(sut.usbDevice.value).isEqualTo(mockUsbDevice1)
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
}