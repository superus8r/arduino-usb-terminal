package org.kabiri.android.usbterminal.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import javax.inject.Inject

internal fun interface IGetCustomBaudRateUseCase {
    suspend operator fun invoke(): Flow<Int>
}

internal class GetCustomBaudRateUseCase
@Inject constructor(
    private val userSettingRepository: IUserSettingRepository,
): IGetCustomBaudRateUseCase {
    override suspend fun invoke(): Flow<Int> {
        val userSettingFlow = userSettingRepository.preferenceFlow
        return userSettingFlow.map { it.baudRate }
    }

}