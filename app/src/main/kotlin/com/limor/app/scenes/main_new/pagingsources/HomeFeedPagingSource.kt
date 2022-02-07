package com.limor.app.scenes.main_new.pagingsources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.limor.app.BuildConfig
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToUIModel

class HomeFeedPagingSource(
    private val repository: GeneralInfoRepository
) : PagingSource<Int, CastUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CastUIModel> {
        val position = params.key ?: STARTING_PAGE_INDEX
        val offset = (position - 1) * params.loadSize

        return try {

            val startTime = System.currentTimeMillis()
            val result = repository.fetchHomeFeed(params.loadSize, offset).map { cast ->
                cast.mapToUIModel()
            }
            val delta = System.currentTimeMillis() - startTime;

            if (BuildConfig.DEBUG) {
                println("Loaded ${result.size} casts from offset $offset and limit of ${params.loadSize} in $delta ms.")
            }

            val nextKey = if (result.size < params.loadSize)
                null
            else
                position + (params.loadSize / NETWORK_PAGE_SIZE)

            LoadResult.Page(
                data = result,
                prevKey = params.key,
                nextKey = nextKey
            )

        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    companion object {
        private const val STARTING_PAGE_INDEX = 1
        const val NETWORK_PAGE_SIZE = 10
    }

    override fun getRefreshKey(state: PagingState<Int, CastUIModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}