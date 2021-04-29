package com.limor.app.scenes.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val viewModelJob = Job()

private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
private val backgroundScope = CoroutineScope(Dispatchers.IO + viewModelJob)

fun <P> BACKGROUND(action: suspend () -> P, onPost: (it: P?) -> Unit = {}) {
    backgroundScope.launch {
        val result: P? = action()
        onPost(result)
    }
}

fun MAIN(action: () -> Unit) {
    uiScope.launch {
        action()
    }
}