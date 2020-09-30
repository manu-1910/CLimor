package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWPopularPodcastsResponse>.asDataEntity(): Single<PopularPodcastsResponseEntity> {
    return this.map { it.asDataEntity() }
}

fun NWPopularPodcastsResponse.asDataEntity(): PopularPodcastsResponseEntity {
    return PopularPodcastsResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWPopularPodcastsArray.asDataEntity(): PopularPodcastsArrayEntity {
    return PopularPodcastsArrayEntity(
        getPopularPodcastsEntities(podcasts)
    )
}

fun getPopularPodcastsEntities(nwList: ArrayList<NWPodcast>?): ArrayList<PodcastEntity> {
    val entityList = ArrayList<PodcastEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}