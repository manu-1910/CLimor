package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWCreatePodcastRecastResponse>.asDataEntity(): Single<CreatePodcastRecastResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreatePodcastRecastResponse.asDataEntity(): CreatePodcastRecastResponseEntity {
    return CreatePodcastRecastResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun CreatePodcastRecastResponseEntity.asRemoteEntity(): NWCreatePodcastRecastResponse {
    return NWCreatePodcastRecastResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWPodcastCreateRecastData.asDataEntity(): CreatePodcastRecastData {
    return CreatePodcastRecastData(
        recast?.asDataEntity()
    )
}

fun NWPodcastRecast.asDataEntity(): PodcastRecastEntity {
    return PodcastRecastEntity(
        podcast_id,
        user_id
    )
}


fun CreatePodcastRecastData.asRemoteEntity(): NWPodcastCreateRecastData {
    return NWPodcastCreateRecastData(
        recast?.asRemoteEntity()
    )
}

fun PodcastRecastEntity.asRemoteEntity(): NWPodcastRecast {
    return NWPodcastRecast(
        podcast_id,
        user_id
    )
}