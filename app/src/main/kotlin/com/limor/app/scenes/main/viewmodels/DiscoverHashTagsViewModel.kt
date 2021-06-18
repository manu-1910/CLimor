package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UITags
import com.limor.app.uimodels.UITagsResponse
import com.limor.app.usecases.TagsUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class DiscoverHashTagsViewModel @Inject constructor(private val tagsUseCase: TagsUseCase) :
    BaseViewModel<DiscoverHashTagsViewModel.Input, DiscoverHashTagsViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()

    var searchText: String = ""
    var results: ArrayList<UITags> = ArrayList()

    data class Input(
        val getFeedTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UITagsResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UITagsResponse>()

        input.getFeedTrigger.subscribe({
            tagsUseCase.execute(searchText).subscribe({

                results.clear()
                results.addAll(it.data.tags)
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
                    e.printStackTrace()
//                    val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
//                    val errorResponse = UIErrorResponse(99, dataError.toString())
//                    errorTracker.postValue(errorResponse!!)
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



