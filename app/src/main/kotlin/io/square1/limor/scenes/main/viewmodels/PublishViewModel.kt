package io.square1.limor.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseViewModel
import io.square1.limor.common.SessionManager
import io.square1.limor.common.SingleLiveEvent
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.uimodels.*
import io.square1.limor.usecases.PublishUseCase
import retrofit2.HttpException
import javax.inject.Inject

class PublishViewModel @Inject constructor(private val publishUseCase: PublishUseCase, private val sessionManager: SessionManager) : BaseViewModel<PublishViewModel.Input, PublishViewModel.Output>() {

    var uiPublishRequest = UIPublishRequest(
        podcast = null
    );

    var categorySelected: String = ""
    var categorySelectedId: Int = 0
    var locationSelectedItem: UILocations = UILocations("", 0.0, 0.0, true)

    private val compositeDispose = CompositeDisposable()

    data class Input(
        val publishTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UIPublishResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIPublishResponse>()

        input.publishTrigger.subscribe({
            publishUseCase.execute(uiPublishRequest).subscribe({
                //sessionManager.storeToken(it.data.access_token.token.access_token)
                response.value = it
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? = error.response()?.errorBody()?.parseSuccessResponse(UIErrorResponse.serializer())
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