package com.limor.app.apollo

import com.limor.app.*
import timber.log.Timber
import javax.inject.Inject

class CastsRepository @Inject constructor(private val apollo: Apollo) {

    suspend fun getFeaturedCasts(
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<GetFeaturedCastsQuery.GetFeaturedCast> {
        return apollo.launchQuery(GetFeaturedCastsQuery(limit, offset))
            ?.data?.getFeaturedCasts?.filterNotNull() ?: emptyList()
    }

    suspend fun getTopCasts(
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<GetTopCastsQuery.GetTopCast> {
        return apollo.launchQuery(GetTopCastsQuery(limit, offset))
            ?.data?.getTopCasts?.filterNotNull() ?: emptyList()
    }

    suspend fun getCastsByCategory(
        categoryId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<GetPodcastsByCategoryQuery.GetPodcastsByCategory> {
        return apollo.launchQuery(GetPodcastsByCategoryQuery(categoryId, limit, offset))
            ?.data?.getPodcastsByCategory?.filterNotNull() ?: emptyList()
    }

    suspend fun getCastsByHashtag(
        tagId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<GetPodcastsByHashtagQuery.GetPodcastsByTag> {
        return apollo.launchQuery(GetPodcastsByHashtagQuery(tagId, limit, offset))
            ?.data?.getPodcastsByTag?.filterNotNull() ?: emptyList()
    }

    suspend fun getCastsByUser(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<GetUserPodcastsQuery.GetUserPodcast> {
        return apollo.launchQuery(GetUserPodcastsQuery(userId, limit, offset))
            ?.data?.getUserPodcasts?.filterNotNull() ?: emptyList()
    }

    suspend fun getPatronCastsByUser(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<GetPatronPodcastsQuery.GetPatronCast> {
        return apollo.launchQuery(GetPatronPodcastsQuery(userId, limit, offset))
            ?.data?.getPatronCasts?.filterNotNull() ?: emptyList()
    }

    suspend fun getCastById(
        castId: Int
    ): GetPodcastByIdQuery.GetPodcastById? {
        return apollo.launchQuery(GetPodcastByIdQuery(castId))
            ?.data?.getPodcastById
    }

    suspend fun deleteCastById(id: Int) {

            val query = DeletePodcastMutation(id)
            val result = apollo.mutate(query)
            val reported = result?.data?.deletePodcast?.destroyed
            Timber.d("Delete podcast  -> $reported")

    }

    suspend fun reportCast(s: String, id: Int?) {
        id?.let{
            val query = CreateReportsMutation(s,"Podcast",id)
            val result = apollo.mutate(query)
            val reported = result?.data?.createReports?.reported
            Timber.d("Report Podcast  -> $reported")
        }

    }

    suspend fun getPurchasedCastsByUser(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<GetPurchasedCastsQuery.GetPurchasedCast>{
        return apollo.launchQuery(GetPurchasedCastsQuery(userId, limit, offset))
            ?.data?.getPurchasedCasts?.filterNotNull() ?: emptyList()
    }

    suspend fun getPlaylists(): List<PlaylistsQuery.Data1?>?{
        return apollo.launchQuery(PlaylistsQuery())?.data?.getPlaylists?.data
    }

}
