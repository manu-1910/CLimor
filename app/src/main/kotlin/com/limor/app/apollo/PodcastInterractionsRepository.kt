package com.limor.app.apollo

import com.google.gson.Gson
import com.limor.app.*
import com.limor.app.apollo.Apollo.Companion.LOAD_PORTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PodcastInteractionsRepository @Inject constructor(val apollo: Apollo) {

    suspend fun likePodcast(podcastId: Int): Int? {
        val mutation = LikePodcastMutation(podcastId)
        val result = apollo.mutate(mutation)
        return result?.data?.likePodcast?.podcast_id
    }

    suspend fun unLikePodcast(podcastId: Int): Int {
        val mutation = UnLikePodcastMutation(podcastId)
        val result = apollo.mutate(mutation)
        return podcastId
    }

    suspend fun getPodcastById(podcastId: Int): FeedItemsQuery.Podcast {
        val query = GetPodcastByIdQuery(podcastId)
        val result = apollo.launchQuery(query)
        return withContext(Dispatchers.Default) {
            val gson = Gson()
            val encodedString = gson.toJson(result?.data?.getPodcastById)
            gson.fromJson(encodedString, FeedItemsQuery.Podcast::class.java)
        }
    }

    suspend fun getCommentsByPodcast(
        podcastId: Int,
        offset: Int = 0
    ): List<GetCommentsByPodcastsQuery.GetCommentsByPodcast>? {
        val query = GetCommentsByPodcastsQuery(podcastId, limit = LOAD_PORTION, offset = offset)
        val result = apollo.launchQuery(query)
        return result?.data?.getCommentsByPodcasts?.filterNotNull()
    }
}