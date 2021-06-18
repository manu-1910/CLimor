package com.limor.app.scenes.authentication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.BuildConfig
import com.limor.app.common.BaseViewModel
import com.limor.app.common.Constants
import com.limor.app.common.SessionManager
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIAuthResponse
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UISignUpUser
import com.limor.app.uimodels.UITokenFBRequest
import com.limor.app.usecases.SignInFBUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class SignFBViewModel @Inject constructor(private val signInFBUseCase: SignInFBUseCase, private val sessionManager: SessionManager) : BaseViewModel<SignFBViewModel.Input, SignFBViewModel.Output>() {

    var firstnameViewModel = ""
    var lastnameViewModel = ""
    var emailViewModel = ""
    var passwordViewModel = ""
    var userimageViewModel = ""
    var fbAccessTokenViewModel = ""
    var fbUidViewModel = ""
    var referralCodeViewModel = ""
    var tokenInApp = ""
    var usernameViewModel = ""

    lateinit var userViewModel: UISignUpUser

    private val compositeDispose = CompositeDisposable()

    data class Input(
        val loginFBTrigger: Observable<Unit>
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

        input.loginFBTrigger.subscribe({
            signInFBUseCase.execute(
                UITokenFBRequest(
                    BuildConfig.CLIENT_ID,
                    BuildConfig.CLIENT_SECRET,
                    Constants.GRANT_TYPE_FACEBOOK,
                    fbAccessTokenViewModel,
                    referralCodeViewModel,
                    UISignUpUser(
                        emailViewModel,
                        passwordViewModel,
                        usernameViewModel
                    )
                )
            ).subscribe({
                sessionManager.storeToken(it.data.token.access_token)
                response.value = it
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? = error.response()?.errorBody()?.parseSuccessResponse(
                        UIErrorResponse.serializer())
                    errorTracker.postValue(errorResponse!!)
                } catch (e: Exception) {
//                    val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
//                    val errorResponse = UIErrorResponse(99, dataError.toString())
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