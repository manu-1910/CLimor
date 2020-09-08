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
import io.square1.limor.uimodels.UIDeleteResponse
import io.square1.limor.uimodels.UIErrorData
import io.square1.limor.uimodels.UIErrorResponse
import io.square1.limor.usecases.DeleteCommentLikeUseCase
import retrofit2.HttpException
import javax.inject.Inject

class DeleteCommentLikeViewModel @Inject constructor(private val deleteCommentLikeUseCase: DeleteCommentLikeUseCase) :
    BaseViewModel<DeleteCommentLikeViewModel.Input, DeleteCommentLikeViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()
    var idComment = 0

    data class Input(
        val deleteCommentLikeTrigger: Observable<Unit>
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

        input.deleteCommentLikeTrigger.subscribe({
            deleteCommentLikeUseCase.execute(idComment).subscribe({
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
                    val dataError =
                        UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
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