package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIBlockedUserResponse
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UIUser
import com.limor.app.uimodels.UIUserIDRequest
import com.limor.app.usecases.CreateBlockedUserUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class CreateBlockedUserViewModel @Inject constructor(private val createBlockedUserUseCase: CreateBlockedUserUseCase) :
    BaseViewModel<CreateBlockedUserViewModel.Input, CreateBlockedUserViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()

    var user : UIUser? = null

    data class Input(
        val createBlockedUserTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UIBlockedUserResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIBlockedUserResponse>()

        input.createBlockedUserTrigger.subscribe({
            val request = UIUserIDRequest(user!!.id)
            createBlockedUserUseCase.execute(request).subscribe({
                response.value = it

            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? =
                        error.response()?.errorBody()?.parseSuccessResponse(
                            UIErrorResponse.serializer()
                        )
                    errorTracker.postValue(errorResponse!!)
                } catch (e: Exception) {
                    e.printStackTrace()
//                    val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
//                    val errorResponse = UIErrorResponse(99, dataError.toString())
//                    errorTracker.postValue(errorResponse!!)
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