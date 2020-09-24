package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*

fun Single<NWGetPodcastsResponse>.asDataEntity(): Single<GetPodcastsResponseEntity> {
    return this.map { it.asDataEntity() }
}

fun NWGetPodcastsResponse.asDataEntity(): GetPodcastsResponseEntity {
    return GetPodcastsResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWPodcastsArray.asDataEntity(): PodcastsItemsEntityArray {
    return PodcastsItemsEntityArray(
        getAllPodcastsItemsEntities(podcasts)
    )
}

fun getAllPodcastsItemsEntities(nwList: ArrayList<NWPodcast>?): ArrayList<PodcastEntity> {
    val entityList = ArrayList<PodcastEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}