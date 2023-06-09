package com.limor.app.apollo

import android.util.Log
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

    suspend fun getFeaturedPodCastsGroups(
        type: String
    ): FeaturedPodcastsGroupsQuery.Data1? {
        return apollo.launchQuery(FeaturedPodcastsGroupsQuery(type))
            ?.data?.getFeaturedPodcastGroups?.data
    }

    suspend fun getFeaturedPodcastsByGroupId(
        groupId: Int
    ): GetFeaturedPodcastsByGroupIdQuery.Data1?{
        Log.d("svsdv","dvverve")
        val result = apollo.launchQuery(GetFeaturedPodcastsByGroupIdQuery(groupId))
        return result?.data?.getFeaturedPodcastsByGroupId?.data
    }

    suspend fun deleteCastById(id: Int) {

        val query = DeletePodcastMutation(id)
        val result = apollo.mutate(query)
        val reported = result?.data?.deletePodcast?.destroyed
        Timber.d("Delete podcast  -> $reported")

    }

    suspend fun reportCast(s: String, id: Int?) {
        id?.let {
            val query = CreateReportsMutation(s, "Podcast", id)
            val result = apollo.mutate(query)
            val reported = result?.data?.createReports?.reported
            Timber.d("Report Podcast  -> $reported")
        }

    }

    suspend fun getPurchasedCastsByUser(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<GetPurchasedCastsQuery.GetPurchasedCast> {
        return apollo.launchQuery(GetPurchasedCastsQuery(userId, limit, offset))
            ?.data?.getPurchasedCasts?.filterNotNull() ?: emptyList()
    }

    suspend fun getPlaylists(): List<PlaylistsQuery.Data1?>? {
        return apollo.launchQuery(PlaylistsQuery())?.data?.getPlaylists?.data
    }

    suspend fun getCastsInPlaylist(
        playlistId: Int
    ): List<GetCastsInPlaylistsQuery.Data1?> {
        return apollo.launchQuery(GetCastsInPlaylistsQuery(playlistId))?.data?.getCastsInPlaylist?.data
            ?: emptyList()
    }

    suspend fun deleteCastInPlaylist(
        playlistId: Int,
        castId: Int
    ): String? {
        return apollo.mutate(DeleteCastInPlaylistMutation(playlistId, castId))
            ?.data?.deleteCastInPlaylist?.status
    }

    suspend fun deletePlaylist(
        playlistId: Int
    ): String? {
        return apollo.mutate(DeletePlaylistMutation(playlistId))
            ?.data?.deletePlaylist?.status
    }

    suspend fun createPlaylist(
        title: String,
        podcastId: Int
    ): String? {
        return apollo.mutate(CreatePlaylistMutation(title, podcastId))
            ?.data?.createPlaylist?.status
    }

    suspend fun editPlaylist(
        title: String,
        playlistId: Int
    ): String? {
        return apollo.mutate(UpdatePlaylistMutation(playlistId, title))?.data?.updatePlaylist?.status
    }

    suspend fun getCastsOfPlaylist(
        podcastId: Int
    ): List<GetPlaylistsOfCastsQuery.Data1?>?{
        return apollo.launchQuery(GetPlaylistsOfCastsQuery(podcastId))?.data?.getPlaylists?.data
    }

    suspend fun addCastToPlaylist(
        podcastId: Int,
        playlistIds: ArrayList<Int>
    ): String? {
        return apollo.mutate(AddCastToPlaylistsMutation(podcastId, playlistIds))?.data?.addCastToPlaylists?.status
    }

    suspend fun handleCastInPlaylists(
        podcastId: Int,
        addedIds: ArrayList<Int>,
        deletedIds: ArrayList<Int>
    ): String? {
        return apollo.mutate(HandleCastInPlaylistsMutation(podcastId, addedIds, deletedIds))?.data?.handleCastInPlaylists?.status
    }

}
