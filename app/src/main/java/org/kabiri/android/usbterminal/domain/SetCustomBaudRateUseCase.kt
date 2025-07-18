package org.kabiri.android.usbterminal.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import javax.inject.Inject

internal fun interface ISetCustomBaudRateUseCase {
    operator fun invoke(baudRate: Int)
}

internal class SetCustomBaudRateUseCase
@Inject constructor(
    private val userSettingRepository: IUserSettingRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
): ISetCustomBaudRateUseCase {

    override fun invoke(baudRate: Int) {
        scope.launch {
            userSettingRepository.setBaudRate(baudRate)
        }
    }

}