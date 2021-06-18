package com.limor.app.scenes.authentication.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.usecases.ForgotPasswordUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject


class ForgotPasswordViewModel @Inject constructor(
    private val forgotPassUseCase: ForgotPasswordUseCase
) : BaseViewModel<ForgotPasswordViewModel.Input, ForgotPasswordViewModel.Output>() {
    var emailForgotViewModel: String = ""
    private val compositeDispose = CompositeDisposable()

    data class Input(
        val trigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<Boolean>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<Boolean>()

        input.trigger.subscribe({
            forgotPassUseCase.execute(emailForgotViewModel).subscribe({
                response.value = true
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? =
                        error.response()?.errorBody()?.parseSuccessResponse(UIErrorResponse.serializer())

                    errorTracker.postValue(errorResponse!!)
                } catch (e: Exception) {
//                    val errorResponse = UIErrorResponse(998, com.limor.app.App.Companion.instance.getString(R.string.some_error))
//                    errorTracker.postValue(errorResponse!!)
                    e.printStackTrace()
                }
            })
        }, {}).addTo(compositeDispose)

        return Output(response, backgroundWorkingProgress, errorTracker)
    }

    override fun onCleared() {
        if (!compositeDispose.isDisposed) compositeDispose.dispose()
        super.onCleared()
    }
}
