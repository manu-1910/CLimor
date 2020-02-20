package io.square1.limor.scenes.authentication

import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.square1.limor.common.BaseViewModel
import io.square1.limor.common.SessionManager
import io.square1.limor.common.SingleLiveEvent
import javax.inject.Inject

class SignInSignUpViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : BaseViewModel<SignInSignUpViewModel.Input, SignInSignUpViewModel.Output>() {
    override fun transform(input: Input): Output {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    data class Input(
        val emailObservable: Observable<String>,
        val passwordObservable: Observable<String>,
        val singInTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<Boolean>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<Int>
    )
}

