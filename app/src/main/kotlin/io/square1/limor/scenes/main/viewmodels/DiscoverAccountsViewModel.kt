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
import io.square1.limor.uimodels.*
import io.square1.limor.usecases.SearchUsersUseCase
import retrofit2.HttpException
import javax.inject.Inject


class DiscoverAccountsViewModel @Inject constructor(private val searchUsersUseCase: SearchUsersUseCase) :
    BaseViewModel<DiscoverAccountsViewModel.Input, DiscoverAccountsViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()

    var limit: Int = 10
    var offset: Int = 0
    var searchText: String = ""
    var results: ArrayList<UIUser> = ArrayList()

    data class Input(
        val getFeedTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UISuggestedUsersResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UISuggestedUsersResponse>()

        input.getFeedTrigger.subscribe({
            searchUsersUseCase.execute(searchText).subscribe({

                results.clear()
                results.addAll(it.data.users)
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
                    val dataError =
                        UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
                    val errorResponse = UIErrorResponse(99, dataError.toString())
                    errorTracker.postValue(errorResponse)
                }

            })
        }, {}).addTo(compositeDispose)

        return Output(response, backgroundWorkingProgress, errorTracker)
    }

    fun updateFollowedStatus(item: UIUser): Int {

        for ((index, user) in results.withIndex()) {
            if (user.id == item.id) {
                user.followed = !item.followed
                return index
            }
        }

        return -1
    }

    override fun onCleared() {
        if (!compositeDispose.isDisposed) compositeDispose.dispose()
        super.onCleared()
    }
}