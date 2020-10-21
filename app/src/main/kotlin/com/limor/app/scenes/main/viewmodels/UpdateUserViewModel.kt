package com.limor.app.scenes.main.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SessionManager
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UIGetUserResponse
import com.limor.app.uimodels.UIUpdateProfileRequest
import com.limor.app.uimodels.UIUpdateUser
import com.limor.app.usecases.ProfileUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class UpdateUserViewModel @Inject constructor(private val profileUseCase: ProfileUseCase, private val sessionManager: SessionManager) : BaseViewModel<UpdateUserViewModel.Input, UpdateUserViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()
    //var updatedUser: UIUpdateUser = UIUpdateUser()

    var first_name: String = ""
    var last_name: String = ""
    var username: String = ""
    var website: String = ""
    var description: String = ""
    var email: String = ""
    var phone_number: String = ""
    var date_of_birth: Long = 0
    var gender: String = ""
    var image = ""
    var notifications_enabled = false


    data class Input(
        val updateUserTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UIGetUserResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIGetUserResponse>()

        input.updateUserTrigger.subscribe({
            profileUseCase.executeUpdate(
                UIUpdateProfileRequest(
                    UIUpdateUser(
                        first_name,
                        last_name,
                        username,
                        website,
                        description,
                        email,
                        phone_number,
                        date_of_birth,
                        gender,
                        notifications_enabled,
                        image_url = image

                )
                )
            ).subscribe({
                //sessionManager.storeToken(it.data.access_token.token.access_token)
                sessionManager.storeUser(it.data.user)
                response.value = it
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? = error.response()?.errorBody()?.parseSuccessResponse(
                        UIErrorResponse.serializer())
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