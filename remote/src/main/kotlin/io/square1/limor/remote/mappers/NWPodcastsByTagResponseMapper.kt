package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*

fun Single<NWPodcastsByTagResponse>.asDataEntity(): Single<PodcastsByTagResponseEntity> {
    return this.map { it.asDataEntity() }
}

fun NWPodcastsByTagResponse.asDataEntity(): PodcastsByTagResponseEntity {
    return PodcastsByTagResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWPodcastsTagItemsArray.asDataEntity(): PodcastsTagItemsEntityArray {
    return PodcastsTagItemsEntityArray(
        getAllPodcastsTagItemsEntities(podcasts)
    )
}

fun getAllPodcastsTagItemsEntities(nwList: ArrayList<NWPodcast>?): ArrayList<PodcastEntity> {
    val entityList = ArrayList<PodcastEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}