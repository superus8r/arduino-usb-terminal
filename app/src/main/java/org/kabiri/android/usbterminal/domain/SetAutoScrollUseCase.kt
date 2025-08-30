package org.kabiri.android.usbterminal.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kabiri.android.usbterminal.data.repository.IUserSettingRepository
import javax.inject.Inject

internal fun interface ISetAutoScrollUseCase {
    operator fun invoke(enabled: Boolean)
}

internal class SetAutoScrollUseCase
    @Inject
    constructor(
        private val userSettingRepository: IUserSettingRepository,
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ) : ISetAutoScrollUseCase {
        override fun invoke(enabled: Boolean) {
            scope.launch {
                userSettingRepository.setAutoScroll(enabled)
            }
        }
    }
