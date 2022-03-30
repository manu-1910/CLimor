package com.limor.app.scenes.main_new.pagingsources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.limor.app.BuildConfig
import com.limor.app.apollo.CastsRepository
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToUIModel

class UserCastsPagingSource(
    private val userId: Int,
    private val castsRepository: CastsRepository,
    private val castsType: Int = 1
) : PagingSource<Int, CastUIModel>() {

    override fun getRefreshKey(state: PagingState<Int, CastUIModel>): Int? {
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

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CastUIModel> {
        val position = params.key ?: UserCastsPagingSource.STARTING_PAGE_INDEX
        val offset = (position - 1) * params.loadSize

        return try {

            val startTime = System.currentTimeMillis()

            val result = when (castsType) {
                PATRON_CASTS -> castsRepository.getPatronCastsByUser(
                    userId,
                    params.loadSize,
                    offset
                ).map { cast ->
                    cast.mapToUIModel()
                }
                USER_CASTS -> castsRepository.getCastsByUser(userId, params.loadSize, offset)
                    .map { cast ->
                        cast.mapToUIModel()
                    }
                else ->
                    castsRepository.getPurchasedCastsByUser(userId, params.loadSize, offset)
                        .map { cast ->
                            cast.mapToUIModel()
                        }
            }

            val delta = System.currentTimeMillis() - startTime;

            if (BuildConfig.DEBUG) {
                println("Loaded ${result.size} casts from offset $offset and limit of ${params.loadSize} in $delta ms.")
            }

            val nextKey = if (result.size < params.loadSize)
                null
            else
                position + (params.loadSize / UserCastsPagingSource.NETWORK_PAGE_SIZE)

            if (BuildConfig.DEBUG) {
                println("Home feed loading with prev key ${params.key} and next $nextKey")
            }

            LoadResult.Page(
                data = result,
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
        const val PURCHASED_CASTS = 3
        const val PATRON_CASTS = 2
        const val USER_CASTS = 1
    }

}