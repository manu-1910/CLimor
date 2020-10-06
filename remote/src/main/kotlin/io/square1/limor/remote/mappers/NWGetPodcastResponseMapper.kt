package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*

fun Single<NWGetPodcastResponse>.asDataEntity(): Single<GetPodcastResponseEntity> {
    return this.map { it.asDataEntity() }
}

fun NWGetPodcastResponse.asDataEntity(): GetPodcastResponseEntity {
    return GetPodcastResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWPodcastData.asDataEntity(): PodcastItemEntity {
    return PodcastItemEntity(
        podcast.asDataEntity()
    )
}