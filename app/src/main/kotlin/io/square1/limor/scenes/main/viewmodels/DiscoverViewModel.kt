package io.square1.limor.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function4
import io.square1.limor.App
import io.square1.limor.BuildConfig
import io.square1.limor.R
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.uimodels.*
import io.square1.limor.usecases.*
import retrofit2.HttpException
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
    private val popularPodcastsUseCase: PopularPodcastsUseCase,
    private val featuredPodcastsUseCase: FeaturedPodcastsUseCase,
    private val suggestedUsersUseCase: SuggestedUsersUseCase,
    private val trendingTagsUseCase: TrendingTagsUseCase
) : ViewModel() {

    private val compositeDispose = CompositeDisposable()

    private var popularPodcasts: ArrayList<UIPodcast> = ArrayList()
    private var featuredPodcasts: ArrayList<UIPodcast> = ArrayList()
    private var suggestedUsers: ArrayList<UIUser> = ArrayList()
    private var promotedTags: ArrayList<UITags> = ArrayList()

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
                DiscoverAllData(popularPodcasts, featuredPodcasts, suggestedUsers, promotedTags)
        }
    }

    fun reload(){
        hasData = false
        getAllData()
    }

    // We need to show a loading spinner until we have data from multiple API sources
    private fun getAllData() {
        val popularSingle = popularPodcastsUseCase.execute()
        val featuredSingle = featuredPodcastsUseCase.execute()
        val suggestedSingle = suggestedUsersUseCase.execute()
        val tagsSingle = trendingTagsUseCase.execute()

        val combined: Observable<AllDataResult> = Observable.zip(
            popularSingle.toObservable(),
            featuredSingle.toObservable(),
            suggestedSingle.toObservable(),
            tagsSingle.toObservable(),

            Function4<UIPopularPodcastsResponse, UIFeaturedPodcastsResponse, UISuggestedUsersResponse, UITagsResponse, AllDataResult>

            { popular, featured, suggested, tags ->

                return@Function4 AllDataResult(
                    popular,
                    featured,
                    suggested,
                    tags
                )
            }
        )

        compositeDispose.add(combined.subscribe({

            popularPodcasts = it.popularPodcastsResponse.data.podcasts
            featuredPodcasts = it.featuredPodcastsResponse.data.podcasts
            suggestedUsers = it.suggestedUsersResponse.data.users
            promotedTags = it.trendingTagsResponse.data.tags

            // Mock some data
            if(promotedTags.size == 0 && BuildConfig.DEBUG){
                for(i in 1..20){
                    promotedTags.add(UITags(i, i.toString() + "Tag", i, false ))
                }
            }

            val allData =
                DiscoverAllData(popularPodcasts, featuredPodcasts, suggestedUsers, promotedTags)
            _discoverState.value = allData

            hasData = true

        }, {

            try {
                val error = it as HttpException
                val errorResponse: UIErrorResponse? =
                    error.response().errorBody()?.parseSuccessResponse(
                        UIErrorResponse.serializer()
                    )
                _discoverState.value = DiscoverError(errorResponse!!)

            } catch (e: Exception) {
                val dataError =
                    UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
                val errorResponse = UIErrorResponse(99, dataError.toString())
                _discoverState.value = DiscoverError(errorResponse)
            }

        }))
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
    val trendingTags: ArrayList<UITags>
) : DiscoverState()

data class DiscoverError(val error: UIErrorResponse) : DiscoverState()
// End of States for the fragment to observe

// Data class to zip up all API responses into on first load
data class AllDataResult(
    val popularPodcastsResponse: UIPopularPodcastsResponse,
    val featuredPodcastsResponse: UIFeaturedPodcastsResponse,
    val suggestedUsersResponse: UISuggestedUsersResponse,
    val trendingTagsResponse: UITagsResponse
)