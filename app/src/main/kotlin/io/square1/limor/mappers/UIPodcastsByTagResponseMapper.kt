package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<PodcastsByTagResponseEntity>.asUIModel(): Single<UIPodcastsByTagResponse> {
    return this.map { it.asUIModel() }
}

fun PodcastsByTagResponseEntity.asUIModel(): UIPodcastsByTagResponse {
    return UIPodcastsByTagResponse(
        code,
        message,
        data.asUIModel()
    )
}


fun PodcastsTagItemsEntityArray.asUIModel(): UIPodcastsTagItemsArray {
    return UIPodcastsTagItemsArray(
        getAllPodcastEntities(podcasts)
    )
}

fun getAllPodcastEntities(entityList: ArrayList<PodcastEntity>?): ArrayList<UIPodcast> {
    val uiList = ArrayList<UIPodcast>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}
