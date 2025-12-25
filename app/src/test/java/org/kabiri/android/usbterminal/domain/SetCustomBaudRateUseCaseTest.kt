package org.kabiri.android.usbterminal.domain

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository

internal class SetCustomBaudRateUseCaseTest {
    private lateinit var sut: SetCustomBaudRateUseCase

    private val mockUserSettingRepository: IUserSettingRepository = mockk()

    @Before
    internal fun setup() {
        sut =
            SetCustomBaudRateUseCase(
                userSettingRepository = mockUserSettingRepository,
            )
    }

    @After
    internal fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `test invoke calls setBaudRate with expected value on repository`() =
        runTest {
            // arrange
            val expected = 1234
            mockUserSettingRepository.apply {
                coEvery { setBaudRate(expected) } returns Unit
            }

            // act
            sut(expected)

            // assert
            coVerify(exactly = 1) { mockUserSettingRepository.setBaudRate(expected) }
        }
}
