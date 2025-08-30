package org.kabiri.android.usbterminal.domain

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import org.kabiri.android.usbterminal.model.UserSettingPreferences

internal class SetAutoScrollUseCaseTest {
    private lateinit var sut: SetAutoScrollUseCase

    private val mockUserSettingRepository: IUserSettingRepository = mockk()

    @Before
    internal fun setup() {
        sut =
            SetAutoScrollUseCase(
                userSettingRepository = mockUserSettingRepository,
            )
    }

    @Test
    fun `test setAutoScrollUseCase calls setAutoScroll with true on repository`() =
        runTest {
            // arrange
            val expected = true
            mockUserSettingRepository.apply {
                coEvery { setAutoScroll(expected) } returns Unit
            }

            // act
            sut(expected)

            // assert
            coVerify(exactly = 1) { mockUserSettingRepository.setAutoScroll(expected) }
        }

    @Test
    fun `test setAutoScrollUseCase calls setAutoScroll with false on repository`() =
        runTest {
            // arrange
            val expected = false
            mockUserSettingRepository.apply {
                coEvery { setAutoScroll(expected) } returns Unit
            }

            // act
            sut(expected)

            // assert
            coVerify(exactly = 1) { mockUserSettingRepository.setAutoScroll(expected) }
        }
}
