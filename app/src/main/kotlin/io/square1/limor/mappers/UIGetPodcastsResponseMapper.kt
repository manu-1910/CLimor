package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*

fun Single<GetPodcastsResponseEntity>.asUIModel(): Single<UIGetPodcastsResponse> {
    return this.map { it.asUIModel() }
}

fun GetPodcastsResponseEntity.asUIModel(): UIGetPodcastsResponse {
    return UIGetPodcastsResponse(
        code,
        message,
        data.asUIModel()
    )
}


fun PodcastsItemsEntityArray.asUIModel(): UIGetPodcastsItemsArray {
    return UIGetPodcastsItemsArray(
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