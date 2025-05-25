package org.kabiri.android.usbterminal.domain

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.kabiri.android.usbterminal.common.getFakeUserSettingRepository
import org.kabiri.android.usbterminal.model.UserSettingPreferences

internal class GetCustomBaudRateUseCaseAndroidTest {

    @Test
    fun testGetCustomBaudRateUseCaseAndroidTestReturns() = runTest {

        // arrange
        val expectedBaudRate = 1234
        val fakeUserSettings = UserSettingPreferences(baudRate = expectedBaudRate)
        val fakeUserSettingRepository =
            getFakeUserSettingRepository(fakeUserSetting = fakeUserSettings)
        val sut = GetCustomBaudRateUseCase(
            userSettingRepository = fakeUserSettingRepository
        )

        // act
        val actualBaudRate = sut().first()

        // assert
        assertThat(actualBaudRate).isEqualTo(expectedBaudRate)
    }
}