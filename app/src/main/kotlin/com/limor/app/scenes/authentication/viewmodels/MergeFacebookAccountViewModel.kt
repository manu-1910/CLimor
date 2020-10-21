package com.limor.app.scenes.authentication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SessionManager
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIAuthResponse
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UIMergeFacebookAccountRequest
import com.limor.app.uimodels.UISignUpUser
import com.limor.app.usecases.MergeFacebookAccountUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class MergeFacebookAccountViewModel @Inject constructor(private val mergeFacebookAccountUseCase: MergeFacebookAccountUseCase, private val sessionManager: SessionManager) : BaseViewModel<MergeFacebookAccountViewModel.Input, MergeFacebookAccountViewModel.Output>() {


    var fbAccessTokenViewModel = ""
    var fbUidViewModel = ""

    lateinit var userViewModel: UISignUpUser

    private val compositeDispose = CompositeDisposable()

    data class Input(
        val mergeFacebookAccountTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UIAuthResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIAuthResponse>()

        input.mergeFacebookAccountTrigger.subscribe({
            mergeFacebookAccountUseCase.execute(
                UIMergeFacebookAccountRequest(
                    fbAccessTokenViewModel,
                    fbUidViewModel
                )
            ).subscribe({
                response.value = it
                sessionManager.storeToken(it.data.token.access_token)
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? = error.response()?.errorBody()?.parseSuccessResponse(
                        UIErrorResponse.serializer())
                    errorTracker.postValue(errorResponse)
                } catch (e: Exception) {
                    e.printStackTrace()
//                    val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
//                    val errorResponse = UIErrorResponse(99, dataError.toString())
//                    errorTracker.postValue(errorResponse)
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