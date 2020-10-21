package com.limor.app.mappers

import com.limor.app.uimodels.PopularPodcastsArray
import com.limor.app.uimodels.UIPopularPodcastsResponse
import entities.response.PopularPodcastsArrayEntity
import entities.response.PopularPodcastsResponseEntity
import io.reactivex.Single


fun Single<PopularPodcastsResponseEntity>.asUIModel(): Single<UIPopularPodcastsResponse> {
    return this.map { it.asUIModel() }
}

fun PopularPodcastsResponseEntity.asUIModel(): UIPopularPodcastsResponse {
    return UIPopularPodcastsResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun PopularPodcastsArrayEntity.asUIModel(): PopularPodcastsArray {
    return PopularPodcastsArray(
        getPodcasts(podcasts)
    )
}

