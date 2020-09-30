package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


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

