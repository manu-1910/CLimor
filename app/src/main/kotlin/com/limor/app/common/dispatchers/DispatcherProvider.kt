package com.limor.app.common.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
    val background: CoroutineDispatcher
}