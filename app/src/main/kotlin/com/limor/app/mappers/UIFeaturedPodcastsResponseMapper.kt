package com.limor.app.mappers

import com.limor.app.uimodels.FeaturedPodcastsArray
import com.limor.app.uimodels.UIFeaturedPodcastsResponse
import com.limor.app.uimodels.UIPodcast
import entities.response.FeaturedPodcastsArrayEntity
import entities.response.FeaturedPodcastsResponseEntity
import entities.response.PodcastEntity
import io.reactivex.Single


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