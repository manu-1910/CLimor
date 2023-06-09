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

    suspend fun recastPodcast(podcastId: Int): CreateRecastMutation.CreateRecast? {
        val mutation = CreateRecastMutation(podcastId)
        val result = apollo.mutate(mutation)
        return result?.data?.createRecast
    }

    suspend fun deleteRecastPodcast(podcastId: Int): DeleteRecastMutation.DeleteRecast?{
        val mutation = DeleteRecastMutation(podcastId)
        val result = apollo.mutate(mutation)
        return result?.data?.deleteRecast
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

    suspend fun sharePodcast(
        podcastId: Int,
        shareCount: Int = 1
    ) : SharePodcastMutation.SharePodcast? {
        val mutation = SharePodcastMutation(podcastId, shareCount)
        val result = apollo.mutate(mutation)
        return result?.data?.sharePodcast
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

    suspend fun listenPodcast(podcastId: Int): Boolean?{
        val mutation = ListenPodcastMutation(podcastId)
        val result = apollo.mutate(mutation)
        return result?.data?.listenPodcast?.listened
    }

    suspend fun listenComment(commentId: Int) {
        val mutation = ListenCommentMutation(commentId)
        val result = apollo.mutate(mutation)
    }

    suspend fun deleteComment(commentId: Int): Boolean? {
        val mutation = DeleteCommentMutation(commentId)
        val result = apollo.mutate(mutation)
        return result?.data?.deleteComment?.destroyed
    }

    suspend fun updateComment(commentId: Int, text: String): String? {
        val mutation = UpdateCommentMutation(commentId, text)
        val result = apollo.mutate(mutation)
        return result?.data?.updateComment?.content
    }

    suspend fun updatePreview(
        podcastId: Int,
        previewDuration: Int,
        startsAt: Int,
        endsAt: Int
    ): Boolean {
        val mutation = UpdatePreviewMutation(podcastId, previewDuration, startsAt, endsAt)
        val result = apollo.mutate(mutation)
        return result?.data?.updateCastPreview?.status == "Success"
    }
}
