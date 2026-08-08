package org.kabiri.android.usbterminal.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal var SharedFlow<String>.value: String
    get() = this.replayCache.lastOrNull() ?: ""
    set(v) {
        (this as MutableSharedFlow<String>).tryEmit(v)
    }
