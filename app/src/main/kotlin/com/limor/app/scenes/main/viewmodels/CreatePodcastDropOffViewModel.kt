package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SingleLiveEvent
import io.square1.limor.remote.extensions.parseSuccessResponse
import com.limor.app.uimodels.*
import com.limor.app.usecases.CreatePodcastDropOffUseCase
import retrofit2.HttpException
import javax.inject.Inject

class CreatePodcastDropOffViewModel @Inject constructor(private val createPodcastDropOffUseCase: CreatePodcastDropOffUseCase) :
    BaseViewModel<CreatePodcastDropOffViewModel.Input, CreatePodcastDropOffViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()
    var idPodcast = 0
    var percentage = 0f

    data class Input(
        val createDropOffTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UIUpdatedResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIUpdatedResponse>()

        input.createDropOffTrigger.subscribe({
            val request = UIDropOffRequest(percentage)
            createPodcastDropOffUseCase.execute(idPodcast, request).subscribe({
                response.value = it

            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? =
                        error.response().errorBody()?.parseSuccessResponse(
                            UIErrorResponse.serializer()
                        )
                    errorTracker.postValue(errorResponse)
                } catch (e: Exception) {
                    e.printStackTrace()
//                    val dataError =
//                        UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
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