package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.uimodels.*
import com.limor.app.usecases.CategoriesUseCase
import com.limor.app.usecases.FeaturedPodcastsUseCase
import com.limor.app.usecases.PopularPodcastsUseCase
import com.limor.app.usecases.SuggestedUsersUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function4
import io.square1.limor.remote.extensions.parseSuccessResponse
import retrofit2.HttpException
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
    private val popularPodcastsUseCase: PopularPodcastsUseCase,
    private val featuredPodcastsUseCase: FeaturedPodcastsUseCase,
    private val suggestedUsersUseCase: SuggestedUsersUseCase,
    private val categoriesUseCase: CategoriesUseCase
) : ViewModel() {

    private val compositeDispose = CompositeDisposable()

    private var popularPodcasts: ArrayList<UIPodcast> = ArrayList()
    private var featuredPodcasts: ArrayList<UIPodcast> = ArrayList()
    private var suggestedUsers: ArrayList<UIUser> = ArrayList()
    private var categories: ArrayList<UICategory> = ArrayList()

    private var hasData = false
    var isSearchConfigChange = false
    var isSearching = false

    private val _discoverState = MutableLiveData<DiscoverState>()
    val discoverState: LiveData<DiscoverState>
        get() = _discoverState

    var limit: Int = 10
    var offset: Int = 0

    fun start() {
        if (!hasData) {
            getAllData()
        } else {
            _discoverState.value =
                DiscoverAllData(popularPodcasts, featuredPodcasts, suggestedUsers, categories)
        }
    }

    fun reload() {
        hasData = false
        getAllData()
    }

    // We need to show a loading spinner until we have data from multiple API sources
    private fun getAllData() {
        val popularSingle = popularPodcastsUseCase.execute()
        val featuredSingle = featuredPodcastsUseCase.execute()
        val suggestedSingle = suggestedUsersUseCase.execute()
        val categoriesSingle = categoriesUseCase.execute()

        val combined: Observable<AllDataResult> = Observable.zip(
            popularSingle.toObservable(),
            featuredSingle.toObservable(),
            suggestedSingle.toObservable(),
            categoriesSingle.toObservable(),

            Function4<UIPopularPodcastsResponse, UIFeaturedPodcastsResponse, UISuggestedUsersResponse, UICategoriesResponse, AllDataResult>

            { popular, featured, suggested, categories ->

                return@Function4 AllDataResult(
                    popular,
                    featured,
                    suggested,
                    categories
                )
            }
        )

        compositeDispose.add(combined.subscribe({

            popularPodcasts = it.popularPodcastsResponse.data.podcasts
            featuredPodcasts = it.featuredPodcastsResponse.data.podcasts
            suggestedUsers = it.suggestedUsersResponse.data.users
            categories = it.categoriesResponse.data.categories


            val allData =
                DiscoverAllData(popularPodcasts, featuredPodcasts, suggestedUsers, categories)
            _discoverState.value = allData

            hasData = true

        }, {

            try {
                val error = it as HttpException
                val errorResponse: UIErrorResponse? =
                    error.response()?.errorBody()?.parseSuccessResponse(
                        UIErrorResponse.serializer()
                    )
                _discoverState.value = DiscoverError(errorResponse!!)

            } catch (e: Exception) {

//                    val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
//                    var errorResponse = UIErrorResponse(99, "Error exception")
//                    errorTracker.postValue(errorResponse)
//                _discoverState.value = DiscoverError(errorResponse)
                e.printStackTrace()
            }

        }))
    }

    fun deleteFeaturedItem(index: Int) {
        featuredPodcasts.removeAt(index)
    }


    override fun onCleared() {
        if (!compositeDispose.isDisposed) compositeDispose.dispose()
        super.onCleared()
    }


}

// States for the fragment to observe
sealed class DiscoverState

data class DiscoverAllData(
    val popularPodcasts: ArrayList<UIPodcast>,
    val featuredPodcasts: ArrayList<UIPodcast>,
    val suggestedUsers: ArrayList<UIUser>,
    val categories: ArrayList<UICategory>
) : DiscoverState()

data class DiscoverError(val error: UIErrorResponse) : DiscoverState()
// End of States for the fragment to observe

// Data class to zip up all API responses into on first load
data class AllDataResult(
    val popularPodcastsResponse: UIPopularPodcastsResponse,
    val featuredPodcastsResponse: UIFeaturedPodcastsResponse,
    val suggestedUsersResponse: UISuggestedUsersResponse,
    val categoriesResponse: UICategoriesResponse
)