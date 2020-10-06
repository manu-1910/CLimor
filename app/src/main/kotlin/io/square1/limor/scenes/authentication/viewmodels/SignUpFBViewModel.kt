package io.square1.limor.scenes.authentication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.BuildConfig
import io.square1.limor.common.BaseViewModel
import io.square1.limor.common.Constants
import io.square1.limor.common.SessionManager
import io.square1.limor.common.SingleLiveEvent
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.uimodels.UIErrorResponse
import io.square1.limor.uimodels.UISignUpFacebookRequest
import io.square1.limor.uimodels.UISignUpFacebookUser
import io.square1.limor.uimodels.UISignUpResponse
import io.square1.limor.usecases.SignUpFBUseCase
import retrofit2.HttpException
import javax.inject.Inject

class SignUpFBViewModel @Inject constructor(private val signUpFBUseCase: SignUpFBUseCase, private val sessionManager: SessionManager) : BaseViewModel<SignUpFBViewModel.Input, SignUpFBViewModel.Output>() {

    var firstnameViewModel = ""
    var lastnameViewModel = ""
    var emailViewModel = ""
    var passwordViewModel = ""
    var userimageViewModel = ""
    var fbAccessTokenViewModel = ""
    var fbUidViewModel = ""
    var userNameViewModel = ""

    private val compositeDispose = CompositeDisposable()

    data class Input(
        val singUpFBTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UISignUpResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UISignUpResponse>()

        input.singUpFBTrigger.subscribe({
            signUpFBUseCase.execute(
                UISignUpFacebookRequest(
                    BuildConfig.CLIENT_ID,
                    BuildConfig.CLIENT_SECRET,
                    Constants.SCOPES,
                    UISignUpFacebookUser(
                        fbUidViewModel,
                        fbAccessTokenViewModel,
                        emailViewModel,
                        passwordViewModel,
                        userNameViewModel
                    )
                )
            ).subscribe({
                sessionManager.storeToken(it.data.access_token.token.access_token)
                response.value = it
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? = error.response()?.errorBody()?.parseSuccessResponse(UIErrorResponse.serializer())
                    errorTracker.postValue(errorResponse)
                } catch (e: Exception) {
                    //val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
                    //val errorResponse = UIErrorResponse(99, dataError.toString())
                    //errorTracker.postValue(errorResponse)
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