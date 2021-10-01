package com.limor.app.apollo

import com.apollographql.apollo.api.Input
import com.limor.app.CreatePodcastMutation
import com.limor.app.UpdatePodcastMutation
import com.limor.app.type.CreatePodcastInput
import timber.log.Timber
import javax.inject.Inject

class PublishRepository @Inject constructor(val apollo: Apollo) {

    suspend fun createPodcast(podcast: CreatePodcastInput): String? {
        val query = CreatePodcastMutation(podcast)
        val queryResult = apollo.mutate(query)
        val createPodcastResult: CreatePodcastMutation.CreatePodcast? =
            queryResult?.data?.createPodcast
        Timber.d("CreatePodcastMutation -> ${createPodcastResult?.status}")
        return createPodcastResult?.status
    }

    suspend fun updatePodcast(podcastId: Int, title: String, caption: String): String?{
        val mutation = UpdatePodcastMutation(podcastId, Input.fromNullable(title), Input.fromNullable(caption))
        val result = apollo.mutate(mutation)
        val updatePodcastResult: UpdatePodcastMutation.UpdatePodcast? =
            result?.data?.updatePodcast
        return updatePodcastResult?.status
    }

}