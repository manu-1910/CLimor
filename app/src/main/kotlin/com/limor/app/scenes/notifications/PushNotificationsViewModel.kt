package com.limor.app.scenes.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UIUserDeviceRequest
import com.limor.app.uimodels.UIUserDeviceResponse
import com.limor.app.usecases.NotificationsUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class PushNotificationsViewModel @Inject constructor(private val notificationsUseCase: NotificationsUseCase) : ViewModel() {

    lateinit var uiUserDeviceRequest: UIUserDeviceRequest
    private val compositeDispose = CompositeDisposable()

    data class Input(
        val postUserDeviceTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UIUserDeviceResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIUserDeviceResponse>()

        input.postUserDeviceTrigger.subscribe({
            notificationsUseCase.execute(uiUserDeviceRequest).subscribe({ uiNotificationsResponse ->
                response.value = uiNotificationsResponse
            }, { exception ->
                try {
                    val error = exception as HttpException
                    val errorResponse: UIErrorResponse? =
                        error.response()?.errorBody()?.parseSuccessResponse(UIErrorResponse.serializer())

                    errorTracker.postValue(errorResponse!!)
                } catch (e: Exception) {
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