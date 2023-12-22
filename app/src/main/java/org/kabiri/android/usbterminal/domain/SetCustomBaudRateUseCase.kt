package org.kabiri.android.usbterminal.domain

import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import javax.inject.Inject

internal fun interface ISetCustomBaudRateUseCase {
    suspend operator fun invoke(baudRate: Int)
}

internal class SetCustomBaudRateUseCase
@Inject constructor(
    private val userSettingRepository: IUserSettingRepository,
): ISetCustomBaudRateUseCase {
    override suspend fun invoke(baudRate: Int) {
        userSettingRepository.setBaudRate(baudRate)
    }

}