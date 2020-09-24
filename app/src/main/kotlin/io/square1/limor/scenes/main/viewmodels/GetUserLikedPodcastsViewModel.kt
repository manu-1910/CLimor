package io.square1.limor.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseViewModel
import io.square1.limor.common.SingleLiveEvent
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.uimodels.UIErrorData
import io.square1.limor.uimodels.UIErrorResponse
import io.square1.limor.uimodels.UIGetPodcastsResponse
import io.square1.limor.usecases.GetUserLikedPodcastsUseCase
import retrofit2.HttpException
import javax.inject.Inject

class GetUserLikedPodcastsViewModel @Inject constructor(private val getUserLikedPodcastsUseCase: GetUserLikedPodcastsUseCase) : BaseViewModel<GetUserLikedPodcastsViewModel.Input, GetUserLikedPodcastsViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()

    var limit: Int = 10
    var offset: Int = 0

    data class Input(
        val getFeedTrigger: Observable<Unit>,
        val idUser: Int
    )

    data class Output(
        val response: LiveData<UIGetPodcastsResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIGetPodcastsResponse>()

        input.getFeedTrigger.subscribe({
            getUserLikedPodcastsUseCase.execute(input.idUser, limit, offset).subscribe({
                response.value = it
                
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? = error.response().errorBody()?.parseSuccessResponse(
                        UIErrorResponse.serializer())
                    errorTracker.postValue(errorResponse)
                } catch (e: Exception) {
                    val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
                    val errorResponse = UIErrorResponse(99, dataError.toString())
                    errorTracker.postValue(errorResponse)
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