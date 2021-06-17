package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UICreateCommentRequest
import com.limor.app.uimodels.UICreateCommentResponse
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.usecases.CreateCommentCommentUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class CreateCommentCommentViewModel @Inject constructor(private val createCommentCommentUseCase: CreateCommentCommentUseCase) :
    BaseViewModel<CreateCommentCommentViewModel.Input, CreateCommentCommentViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()
    var idComment = 0
    lateinit var uiCreateCommentRequest: UICreateCommentRequest

    data class Input(
        val createPodcastCommentTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UICreateCommentResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UICreateCommentResponse>()

        input.createPodcastCommentTrigger.subscribe({
            createCommentCommentUseCase.execute(idComment, uiCreateCommentRequest).subscribe({
                response.value = it

            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? =
                        error.response()?.errorBody()?.parseSuccessResponse(
                            UIErrorResponse.serializer()
                        )
                    errorTracker.postValue(errorResponse!!)
                } catch (e: Exception) {
//                    val dataError =
////                        UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
////                    val errorResponse = UIErrorResponse(99, dataError.toString())
////                    errorTracker.postValue(errorResponse!!)
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