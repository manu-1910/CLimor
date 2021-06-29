package com.limor.app.apollo

import com.limor.app.GetFeaturedCastsQuery
import javax.inject.Inject

class CastsRepository @Inject constructor(private val apollo: Apollo) {

    suspend fun getFeaturedCasts(
        limit: Int = -1,
        offset: Int = 0
    ): List<GetFeaturedCastsQuery.GetFeaturedCast> {
        return apollo.launchQuery(GetFeaturedCastsQuery(limit, offset))
            ?.data?.getFeaturedCasts?.filterNotNull() ?: emptyList()
    }
}