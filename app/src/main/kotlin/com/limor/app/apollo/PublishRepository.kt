package com.limor.app.apollo

import com.limor.app.CreatePodcastMutation
import com.limor.app.type.CreatePodcastInput
import timber.log.Timber

object PublishRepository {

    suspend fun createPodcast(podcast: CreatePodcastInput): String? {
        val query = CreatePodcastMutation(podcast)
        val queryResult = Apollo.mutate(query)
        val createPodcastResult: CreatePodcastMutation.CreatePodcast? =
            queryResult?.data?.createPodcast
        Timber.d("CreatePodcastMutation -> ${createPodcastResult?.status}")
        return createPodcastResult?.status
    }

}