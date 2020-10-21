package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SessionManager
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UILocations
import com.limor.app.uimodels.UILocationsRequest
import com.limor.app.uimodels.UILocationsResponse
import com.limor.app.usecases.LocationsUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class LocationsViewModel @Inject constructor(private val locationsUseCase: LocationsUseCase, private val sessionManager: SessionManager) : BaseViewModel<LocationsViewModel.Input, LocationsViewModel.Output>() {

    var uiLocationsRequest = UILocationsRequest(
        term = ""
    )
    var localListLocations: ArrayList<UILocations> = ArrayList()
    var locationSelectedItem: UILocations = UILocations("", 0.0, 0.0, true)

    private val compositeDispose = CompositeDisposable()

    data class Input(
        val locationsTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UILocationsResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UILocationsResponse>()

        input.locationsTrigger.subscribe({
            locationsUseCase.execute(uiLocationsRequest).subscribe({
                //sessionManager.storeToken(it.data.access_token.token.access_token)
                response.value = it
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