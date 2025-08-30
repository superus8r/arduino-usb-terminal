package org.kabiri.android.usbterminal.domain

import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import org.kabiri.android.usbterminal.model.UserSettingPreferences

internal class GetCustomBaudRateUseCaseTest {
    private lateinit var sut: GetCustomBaudRateUseCase

    private val mockUserSettingRepository: IUserSettingRepository = mockk()

    @Before
    internal fun setup() {
        sut =
            GetCustomBaudRateUseCase(
                userSettingRepository = mockUserSettingRepository,
            )
    }

    @After
    internal fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `test invoke returns expected baudRate`() =
        runTest {
            // arrange
            val expected = 1234
            mockUserSettingRepository.apply {
                coEvery { preferenceFlow } returns
                    flowOf(
                        UserSettingPreferences(baudRate = expected),
                    )
            }

            // act
            val actualBaudRate = sut().first()

            // assert
            assertThat(actualBaudRate).isEqualTo(expected)
        }
}
