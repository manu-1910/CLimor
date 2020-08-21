package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWCreatePodcastLikeResponse>.asDataEntity(): Single<CreatePodcastLikeResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreatePodcastLikeResponse.asDataEntity(): CreatePodcastLikeResponseEntity {
    return CreatePodcastLikeResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun CreatePodcastLikeResponseEntity.asRemoteEntity(): NWCreatePodcastLikeResponse {
    return NWCreatePodcastLikeResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWPodcastCreateLikeData.asDataEntity(): CreatePodcastLikeData {
    return CreatePodcastLikeData(
        like?.asDataEntity()
    )
}

fun NWPodcastLike.asDataEntity(): PodcastLikeEntity {
    return PodcastLikeEntity(
        podcast_id,
        user_id
    )
}


fun CreatePodcastLikeData.asRemoteEntity(): NWPodcastCreateLikeData {
    return NWPodcastCreateLikeData(
        like?.asRemoteEntity()
    )
}

fun PodcastLikeEntity.asRemoteEntity(): NWPodcastLike {
    return NWPodcastLike(
        podcast_id,
        user_id
    )
}