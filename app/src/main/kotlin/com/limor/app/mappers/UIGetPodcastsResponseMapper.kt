package com.limor.app.mappers

import com.limor.app.uimodels.UIGetPodcastsItemsArray
import com.limor.app.uimodels.UIGetPodcastsResponse
import com.limor.app.uimodels.UIPodcast
import entities.response.GetPodcastsResponseEntity
import entities.response.PodcastEntity
import entities.response.PodcastsItemsEntityArray
import io.reactivex.Single


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
