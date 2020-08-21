package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*

fun Single<NWDeletePodcastLikeResponse>.asDataEntity(): Single<DeletePodcastLikeResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWDeletePodcastLikeResponse.asDataEntity(): DeletePodcastLikeResponseEntity {
    return DeletePodcastLikeResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun DeletePodcastLikeResponseEntity.asRemoteEntity(): NWDeletePodcastLikeResponse {
    return NWDeletePodcastLikeResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWPodcastDeleteLikeData.asDataEntity(): DeletePodcastLikeData {
    return DeletePodcastLikeData(
        destroyed
    )
}


fun DeletePodcastLikeData.asRemoteEntity(): NWPodcastDeleteLikeData {
    return NWPodcastDeleteLikeData(
        destroyed
    )
}