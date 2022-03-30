package com.limor.app.scenes.main_new.pagingsources

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.limor.app.BuildConfig
import com.limor.app.apollo.CastsRepository
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.main_new.fragments.DataItem
import com.limor.app.uimodels.FeaturedPodcastGroups
import com.limor.app.uimodels.FeedRecommendedCasts
import com.limor.app.uimodels.FeedSuggestedPeople
import com.limor.app.uimodels.mapToUIModel

class HomeFeedPagingSource(
    private val repository: GeneralInfoRepository,
    private val castsRepository: CastsRepository,
    private val featuredPodcastGroups: FeaturedPodcastGroups?
) : PagingSource<Int, DataItem>() {

    private var lastAppendedItem = RECOMMENDED_CASTS
    private var lastGroupPosition = -1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DataItem> {
        val position = params.key ?: STARTING_PAGE_INDEX
        val offset = (position - 1) * params.loadSize

        return try {
            val startTime = System.currentTimeMillis()

            val finalResult = ArrayList<DataItem>()

            val result = repository.fetchHomeFeed(params.loadSize, offset).map { cast ->
                cast.mapToUIModel()
            }
            finalResult.addAll(result)
            if(result.isNotEmpty()){
                if (lastAppendedItem == RECOMMENDED_CASTS) {
                    val suggestedPeopleResult =
                        repository.getSuggestedPeople(10, 0)?.map { user -> user.mapToUIModel() }
                    suggestedPeopleResult?.let {
                        if (it.isNotEmpty()) {
                            finalResult.add(FeedSuggestedPeople(it))
                        }
                    }
                    lastAppendedItem = SUGGESTED_PEOPLE
                } else {
                    featuredPodcastGroups?.let {
                        var index = lastGroupPosition + 1
                        if(index >= it.count)
                            index = 0
                        lastGroupPosition = index
                        val recommendedCastsResult =
                            castsRepository.getFeaturedPodcastsByGroupId(featuredPodcastGroups.podcastGroups[index].position)?.mapToUIModel()
                        recommendedCastsResult?.let {
                            if(featuredPodcastGroups.podcastGroups.isNotEmpty()){
                                finalResult.add(FeedRecommendedCasts(featuredPodcastGroups.podcastGroups[index].title, recommendedCastsResult))
                            }
                        }
                    }
                    lastAppendedItem = RECOMMENDED_CASTS
                }
            }

            val delta = System.currentTimeMillis() - startTime;

            if (BuildConfig.DEBUG) {
                println("Loaded ${result.size} casts from offset $offset and limit of ${params.loadSize} in $delta ms.")
            }

            val nextKey = if (result.size < params.loadSize)
                null
            else
                position + (params.loadSize / NETWORK_PAGE_SIZE)

            if (BuildConfig.DEBUG) {
                println("Home feed loading with prev key ${params.key} and next $nextKey")
            }

            LoadResult.Page(
                data = finalResult,
                // Only paging forward.
                // https://android-developers.googleblog.com/2020/07/getting-on-same-page-with-paging-3.html
                prevKey = null,
                nextKey = nextKey
            )

        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    companion object {
        private const val STARTING_PAGE_INDEX = 1
        const val NETWORK_PAGE_SIZE = 10
        const val SUGGESTED_PEOPLE = "SUGGESTED_PEOPLE"
        const val RECOMMENDED_CASTS = "RECOMMENDED_CASTS"
    }

    override fun getRefreshKey(state: PagingState<Int, DataItem>): Int? {
        if (BuildConfig.DEBUG) {
            val closest = state.closestPageToPosition(0)
            println("getRefreshKey.state.anchorPosition -> ${state.anchorPosition}, closest prev ${closest?.prevKey}, next ${closest?.nextKey}")
        }
        val next = state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

        return if (next != null && next < 4) {
            null
        } else {
            next
        }
    }
}