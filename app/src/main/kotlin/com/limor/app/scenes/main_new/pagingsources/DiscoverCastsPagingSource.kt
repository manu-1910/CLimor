package com.limor.app.scenes.main_new.pagingsources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.limor.app.BuildConfig
import com.limor.app.apollo.CastsRepository
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToUIModel

class DiscoverCastsPagingSource(
    private val repository: CastsRepository,
    private val categoryId: Int
) : PagingSource<Int, List<CastUIModel>>(){

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, List<CastUIModel>> {
        val position = params.key ?: STARTING_PAGE_INDEX
        val offset = (position - 1) * params.loadSize

        return try {
            val startTime = System.currentTimeMillis()

            val result = repository.getCastsByCategory(categoryId, params.loadSize, offset).map { cast ->
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

            if (BuildConfig.DEBUG) {
                println("Home feed loading with prev key ${params.key} and next $nextKey")
            }

            LoadResult.Page(
                data = listOf(result),
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
        const val NETWORK_PAGE_SIZE = 5
    }

    override fun getRefreshKey(state: PagingState<Int, List<CastUIModel>>): Int? {
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