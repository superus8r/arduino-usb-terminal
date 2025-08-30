package org.kabiri.android.usbterminal.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import javax.inject.Inject

internal interface IGetAutoScrollUseCase {
    operator fun invoke(): Flow<Boolean>
}

internal class GetAutoScrollUseCase
    @Inject
    constructor(
        private val userSettingRepository: IUserSettingRepository,
    ) : IGetAutoScrollUseCase {
        override fun invoke(): Flow<Boolean> {
            val userSettingFlow = userSettingRepository.preferenceFlow
            return userSettingFlow.map { it.autoScroll }
        }
    }
