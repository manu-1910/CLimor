package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UIGetPodcastsResponse
import com.limor.app.usecases.GetPodcastsByCategoryUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class GetPodcastsByCategoryViewModel @Inject constructor(private val getPodcastsByCategoryUseCase: GetPodcastsByCategoryUseCase) :
    BaseViewModel<GetPodcastsByCategoryViewModel.Input, GetPodcastsByCategoryViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()

    var limit: Int = 10
    var offset: Int = 0

    data class Input(
        val trigger: Observable<Unit>,
        val idCategory: Int
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

        input.trigger.subscribe({
            getPodcastsByCategoryUseCase.execute(input.idCategory, limit, offset).subscribe({
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