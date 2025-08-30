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

internal class GetAutoScrollUseCaseTest {
    private lateinit var sut: GetAutoScrollUseCase

    private val mockUserSettingRepository: IUserSettingRepository = mockk()

    @Before
    internal fun setup() {
        sut =
            GetAutoScrollUseCase(
                userSettingRepository = mockUserSettingRepository,
            )
    }

    @After
    internal fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `test getAutoScrollUseCase returns true`() =
        runTest {
            // arrange
            val expected = true
            mockUserSettingRepository.apply {
                coEvery { preferenceFlow } returns
                    flowOf(
                        UserSettingPreferences(autoScroll = expected),
                    )
            }

            // act
            val actual = sut().first()

            // assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `test getAutoScrollUseCase returns false`() =
        runTest {
            // arrange
            val expected = false
            mockUserSettingRepository.apply {
                coEvery { preferenceFlow } returns
                    flowOf(
                        UserSettingPreferences(autoScroll = expected),
                    )
            }

            // act
            val actual = sut().first()

            // assert
            assertThat(actual).isEqualTo(expected)
        }
}
