package org.kabiri.android.usbterminal.domain

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.kabiri.android.usbterminal.common.getFakeUserSettingRepository

@OptIn(ExperimentalCoroutinesApi::class)
internal class SetCustomBaudRateUseCaseAndroidTest {

    @Test
    fun testSetCustomBaudRateUseCaseAndroidTestCallsSetBaudRateOnRepository() = runTest {

        // arrange
        var isCalledSetBaudRate = false
        val fakeUserSettingRepository = getFakeUserSettingRepository(onSetBaudRate = {
            isCalledSetBaudRate = true
        })
        val sut = SetCustomBaudRateUseCase(
            userSettingRepository = fakeUserSettingRepository
        )

        // act
        sut(123)

        // assert
        assertThat(isCalledSetBaudRate).isTrue()
    }
}