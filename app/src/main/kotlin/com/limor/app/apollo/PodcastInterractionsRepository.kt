package com.limor.app.apollo

import com.apollographql.apollo.api.Input
import com.google.gson.Gson
import com.limor.app.*
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

    suspend fun recastPodcast(podcastId: Int): Int? {
        val mutation = CreateRecastMutation(podcastId)
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
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<GetCommentsByPodcastsQuery.GetCommentsByPodcast> {
        val query = GetCommentsByPodcastsQuery(podcastId, limit = limit, offset = offset)
        val result = apollo.launchQuery(query)
        return result?.data?.getCommentsByPodcasts?.filterNotNull() ?: emptyList()
    }

    suspend fun createComment(
        podcastId: Int,
        content: String,
        ownerId: Int,
        ownerType: String,
        audioURI: String? = null,
        duration: Int? = null
    ): Int? {
        val mutation = CreateCommentMutation(
            podcastId = podcastId,
            content = content,
            ownerId = ownerId,
            ownerType = ownerType,
            audioURL = Input.fromNullable(audioURI),
            duration = Input.fromNullable(duration)
        )
        val result = apollo.mutate(mutation)
        return result?.data?.createComment?.id
    }

    suspend fun getCommentById(
        commentId: Int
    ): GetCommentsByIdQuery.GetCommentsById? {
        val query = GetCommentsByIdQuery(commentId)
        val result = apollo.launchQuery(query)
        return result?.data?.getCommentsById
    }

    suspend fun likeComment(commentId: Int): Int? {
        val mutation = LikeCommentMutation(commentId)
        val result = apollo.mutate(mutation)
        return result?.data?.likeComment?.comment_id
    }

    suspend fun unLikeComment(commentId: Int): Int? {
        val mutation = UnLikeCommentMutation(commentId)
        val result = apollo.mutate(mutation)
        return result?.data?.unLikeComment?.comment_id
    }
}
