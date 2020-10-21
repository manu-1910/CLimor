package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIComment
import com.limor.app.uimodels.UIContentRequest
import com.limor.app.uimodels.UIDeleteResponse
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.usecases.DeleteCommentUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class DeleteCommentViewModel @Inject constructor(private val deleteCommentUseCase: DeleteCommentUseCase) :
    BaseViewModel<DeleteCommentViewModel.Input, DeleteCommentViewModel.Output>() {

    lateinit var comment: UIComment
    private val compositeDispose = CompositeDisposable()
    var content : String? = ""

    data class Input(
        val deleteCommentTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UIDeleteResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIDeleteResponse>()

        input.deleteCommentTrigger.subscribe({
            val request = UIContentRequest(content)
            deleteCommentUseCase.execute(comment.id, request).subscribe({
                response.value = it

            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? =
                        error.response()?.errorBody()?.parseSuccessResponse(
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