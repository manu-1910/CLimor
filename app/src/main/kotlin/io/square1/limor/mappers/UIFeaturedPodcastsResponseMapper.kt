package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.FeaturedPodcastsArray
import io.square1.limor.uimodels.UIFeaturedPodcastsResponse
import io.square1.limor.uimodels.UIPodcast


fun Single<FeaturedPodcastsResponseEntity>.asUIModel(): Single<UIFeaturedPodcastsResponse> {
    return this.map { it.asUIModel() }
}

fun FeaturedPodcastsResponseEntity.asUIModel(): UIFeaturedPodcastsResponse {
    return UIFeaturedPodcastsResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun FeaturedPodcastsArrayEntity.asUIModel(): FeaturedPodcastsArray {
    return FeaturedPodcastsArray(
        getPodcasts(podcasts)
    )
}

fun getPodcasts(entityList: ArrayList<PodcastEntity>?): ArrayList<UIPodcast> {
    val podcastList = ArrayList<UIPodcast>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                podcastList.add(item.asUIModel())
        }
    }
    return podcastList
}