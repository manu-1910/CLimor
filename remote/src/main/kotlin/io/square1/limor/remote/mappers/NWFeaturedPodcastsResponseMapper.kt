package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWFeaturedPodcastsResponse>.asDataEntity(): Single<FeaturedPodcastsResponseEntity> {
    return this.map { it.asDataEntity() }
}

fun NWFeaturedPodcastsResponse.asDataEntity(): FeaturedPodcastsResponseEntity {
    return FeaturedPodcastsResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWFeaturedPodcastsArray.asDataEntity() : FeaturedPodcastsArrayEntity {
    return FeaturedPodcastsArrayEntity(
        getFeaturedPodcastsEntities(podcasts)
    )
}

fun getFeaturedPodcastsEntities(nwList: ArrayList<NWPodcast>?): ArrayList<PodcastEntity> {
    val entityList = ArrayList<PodcastEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}