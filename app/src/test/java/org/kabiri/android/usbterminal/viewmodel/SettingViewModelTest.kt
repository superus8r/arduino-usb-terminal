package org.kabiri.android.usbterminal.viewmodel

import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.domain.IGetAutoScrollUseCase
import org.kabiri.android.usbterminal.domain.IGetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.domain.ISetAutoScrollUseCase
import org.kabiri.android.usbterminal.domain.ISetCustomBaudRateUseCase
import org.kabiri.android.usbterminal.ui.setting.SettingViewModel

internal class SettingViewModelTest {
    private val mockGetBaudRate: IGetCustomBaudRateUseCase = mockk()
    private val mockSetBaudRate: ISetCustomBaudRateUseCase = mockk()
    private val mockGetAutoScroll: IGetAutoScrollUseCase = mockk()
    private val mockSetAutoScroll: ISetAutoScrollUseCase = mockk()

    private lateinit var sut: SettingViewModel

    @Before
    internal fun setup() {
        sut =
            SettingViewModel(
                getBaudRate = mockGetBaudRate,
                setBaudRate = mockSetBaudRate,
                getAutoScroll = mockGetAutoScroll,
                setAutoScroll = mockSetAutoScroll,
            )
    }

    @After
    internal fun cleanUp() {
        clearAllMocks()
    }

    @Test
    internal fun `test currentBaudRate calls getBaudRate`() =
        runTest {
            // arrange
            val expected = 123
            every { mockGetBaudRate() } returns flowOf(expected)

            // act
            val actual = sut.currentBaudRate.first()

            // assert
            assertThat(actual).isEqualTo(expected)
            verify(exactly = 1) {
                @Suppress("UnusedFlow")
                mockGetBaudRate()
            }
        }

    @Test
    internal fun `test setNewBaudRate calls setBaudRate`() {
        // arrange
        val expected = 123
        every { mockSetBaudRate(expected) } returns Unit

        // act
        sut.setNewBaudRate(expected)

        // assert
        verify(exactly = 1) { mockSetBaudRate(expected) }
    }

    @Test
    internal fun `test currentAutoScroll calls getAutoScroll`() =
        runTest {
            // arrange
            val expected = true
            every { mockGetAutoScroll() } returns flowOf(expected)

            // act
            val actual = sut.currentAutoScroll.first()

            // assert
            assertThat(actual).isEqualTo(expected)
            verify(exactly = 1) {
                @Suppress("UnusedFlow")
                mockGetAutoScroll()
            }
        }

    @Test
    internal fun `test setAutoScrollEnabled calls setAutoScroll`() {
        // arrange
        val expected = true
        every { mockSetAutoScroll(expected) } returns Unit

        // act
        sut.setAutoScrollEnabled(expected)

        // assert
        verify(exactly = 1) { mockSetAutoScroll(expected) }
    }

    @Test
    internal fun `test resetDefault calls setBaudRate and setAutoScroll`() {
        // arrange
        val expectedBaudRate = 9600
        val expectedAutoScroll = true
        every { mockSetBaudRate(expectedBaudRate) } returns Unit
        every { mockSetAutoScroll(expectedAutoScroll) } returns Unit

        // act
        sut.resetDefault()

        // assert
        verify(exactly = 1) { mockSetBaudRate(expectedBaudRate) }
        verify(exactly = 1) { mockSetAutoScroll(expectedAutoScroll) }
    }
}
