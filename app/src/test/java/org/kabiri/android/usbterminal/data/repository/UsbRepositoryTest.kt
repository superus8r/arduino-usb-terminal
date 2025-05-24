package org.kabiri.android.usbterminal.data.repository

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class UsbRepositoryTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val mockContext: Context = mockk(relaxed = true)
    private val mockUsbManager: UsbManager = mockk(relaxed = true)

    private lateinit var sut: UsbRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `scanForArduinoDevices returns list of usb devices from usbManager`() = runTest {
        // arrange
        val mockUsbDevice1: UsbDevice = mockk()
        val mockUsbDevice2: UsbDevice = mockk()
        val expectedDeviceMap: HashMap<String, UsbDevice> = hashMapOf(
            "device1" to mockUsbDevice1,
            "device2" to mockUsbDevice2
        )
        val expectedDeviceList: List<UsbDevice> = expectedDeviceMap.values.toList()
        every { mockContext.getSystemService(UsbManager::class.java) } returns mockUsbManager
        every { mockUsbManager.deviceList } returns expectedDeviceMap
        sut = UsbRepository(mockContext)

        // act
        val actualDeviceList = sut.scanForArduinoDevices()
        advanceUntilIdle()

        // assert
        verify { mockUsbManager.deviceList }
        assertThat(actualDeviceList).isNotEmpty()
        assertThat(actualDeviceList).hasSize(actualDeviceList.size)
        assertThat(actualDeviceList.containsAll(expectedDeviceList)).isTrue()
    }
}