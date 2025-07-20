package org.kabiri.android.usbterminal.viewmodel

import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.domain.IArduinoUseCase
import org.kabiri.android.usbterminal.domain.IUsbUseCase
import org.kabiri.android.usbterminal.util.IResourceProvider

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainActivityViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    val mockArduinoUsecase: IArduinoUseCase = mockk(relaxed = true)
    val mockUsbUseCase: IUsbUseCase = mockk(relaxed = true)
    val mockResourceProvider: IResourceProvider = mockk(relaxed = true)

    private lateinit var sut: MainActivityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sut = MainActivityViewModel(
            arduinoUseCase = mockArduinoUsecase,
            usbUseCase = mockUsbUseCase,
            resourceProvider = mockResourceProvider,
        )
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        unmockkStatic("org.kabiri.android.usbterminal.util.UsbDeviceExtensionsKt")
        clearAllMocks()
    }

    @Test
    fun `connect emits expected message when device list is empty`() {
        // arrange
        val expected = "this is an error"
        every { mockUsbUseCase.scanForUsbDevices() } returns emptyList()
        every { mockResourceProvider.getString(R.string.helper_error_usb_devices_not_attached) } returns expected

        // act
        sut.connect()

        // assert
        assertThat(sut.errorMessage.value).isEqualTo(expected)
    }
}