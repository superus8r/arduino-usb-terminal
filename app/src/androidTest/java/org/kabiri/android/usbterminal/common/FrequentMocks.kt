package org.kabiri.android.usbterminal.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import org.kabiri.android.usbterminal.model.UserSettingPreferences

internal fun getFakeUserSettingRepository(
    onSetBaudRate: () -> Unit = {},
    onSetAutoScroll: () -> Unit = {},
    fakeUserSetting: UserSettingPreferences =
        UserSettingPreferences(
            baudRate = 123,
            autoScroll = true,
        ),
): IUserSettingRepository =
    object : IUserSettingRepository {
        override val preferenceFlow: Flow<UserSettingPreferences>
            get() = flowOf(fakeUserSetting)

        override suspend fun setBaudRate(baudRate: Int) {
            onSetBaudRate()
        }

        override suspend fun setAutoScroll(enabled: Boolean) {
            onSetAutoScroll()
        }

        override suspend fun clear() {}

        override suspend fun fetchInitialPreferences(): UserSettingPreferences = fakeUserSetting
    }
