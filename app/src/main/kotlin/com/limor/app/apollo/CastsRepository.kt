package com.limor.app.apollo

import com.limor.app.GetFeaturedCastsQuery
import com.limor.app.GetPodcastsByCategoryQuery
import com.limor.app.GetPodcastsByHashtagQuery
import com.limor.app.GetTopCastsQuery
import javax.inject.Inject

class CastsRepository @Inject constructor(private val apollo: Apollo) {

    suspend fun getFeaturedCasts(
        limit: Int = -1,
        offset: Int = 0
    ): List<GetFeaturedCastsQuery.GetFeaturedCast> {
        return apollo.launchQuery(GetFeaturedCastsQuery(limit, offset))
            ?.data?.getFeaturedCasts?.filterNotNull() ?: emptyList()
    }

    suspend fun getTopCasts(
        limit: Int = -1,
        offset: Int = 0
    ): List<GetTopCastsQuery.GetTopCast> {
        return apollo.launchQuery(GetTopCastsQuery(limit, offset))
            ?.data?.getTopCasts?.filterNotNull() ?: emptyList()
    }

    suspend fun getCastsByCategory(
        categoryId: Int,
        limit: Int = -1,
        offset: Int = 0
    ): List<GetPodcastsByCategoryQuery.GetPodcastsByCategory> {
        return apollo.launchQuery(GetPodcastsByCategoryQuery(categoryId, limit, offset))
            ?.data?.getPodcastsByCategory?.filterNotNull() ?: emptyList()
    }

    suspend fun getCastsByHashtag(
        tagId: Int,
        limit: Int = -1,
        offset: Int = 0
    ): List<GetPodcastsByHashtagQuery.GetPodcastsByTag> {
        return apollo.launchQuery(GetPodcastsByHashtagQuery(tagId, limit, offset))
            ?.data?.getPodcastsByTag?.filterNotNull() ?: emptyList()
    }
}
