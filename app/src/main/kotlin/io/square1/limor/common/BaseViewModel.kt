package io.square1.limor.common

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<Input, Output>: ViewModel() {
    abstract fun transform(input: Input): Output
}