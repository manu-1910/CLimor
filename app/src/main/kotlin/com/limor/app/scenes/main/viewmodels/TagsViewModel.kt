package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.limor.app.common.BaseViewModel
import com.limor.app.common.SessionManager
import com.limor.app.common.SingleLiveEvent
import com.limor.app.scenes.main.fragments.discover.search.DiscoverSearchViewModel
import com.limor.app.uimodels.TagUIModel
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UITagsResponse
import com.limor.app.usecases.SearchHashtagsUseCase
import com.limor.app.usecases.TagsUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.remote.extensions.parseSuccessResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class TagsViewModel @Inject constructor(private val tagsUseCase: TagsUseCase, private val sessionManager: SessionManager,
                                        private val searchHashtagsUseCase: SearchHashtagsUseCase,
) : BaseViewModel<TagsViewModel.Input, TagsViewModel.Output>() {


    private val compositeDispose = CompositeDisposable()
    var tagToSearch = ""

    private val _tagSearchResult = MutableLiveData<List<TagUIModel>>()
    val searchResult: LiveData<List<TagUIModel>> = _tagSearchResult

    data class Input(
        val categoriesTrigger: Observable<Unit>
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

       /* input.categoriesTrigger.subscribe({
            tagsUseCase.execute(tagToSearch).subscribe({
                response.value = it
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? = error.response()?.errorBody()?.parseSuccessResponse(
                        UIErrorResponse.serializer())
                    errorTracker.postValue(errorResponse!!)
                } catch (e: Exception) {
//                    val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
//                    val errorResponse = UIErrorResponse(99, dataError.toString())
//                    errorTracker.postValue(errorResponse!!)
                }

            })
        }, {}).addTo(compositeDispose)*/

        return Output(response, backgroundWorkingProgress, errorTracker)
    }

    override fun onCleared() {
        if (!compositeDispose.isDisposed) compositeDispose.dispose()
        super.onCleared()
    }

    fun searchHashTags(searchQuery: String){
        viewModelScope.launch {
            searchHashtagsUseCase.execute(searchQuery)
                .onSuccess { _tagSearchResult.value = it}
                .onFailure { Timber.e(it, "Error while searching for hashtags") }
        }

    }
}