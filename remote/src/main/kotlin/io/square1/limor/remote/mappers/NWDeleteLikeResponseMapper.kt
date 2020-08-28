package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*

fun Single<NWDeleteLikeResponse>.asDataEntity(): Single<DeleteLikeResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWDeleteLikeResponse.asDataEntity(): DeleteLikeResponseEntity {
    return DeleteLikeResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun DeleteLikeResponseEntity.asRemoteEntity(): NWDeleteLikeResponse {
    return NWDeleteLikeResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWDeleteLikeData.asDataEntity(): DeleteLikeData {
    return DeleteLikeData(
        destroyed
    )
}


fun DeleteLikeData.asRemoteEntity(): NWDeleteLikeData {
    return NWDeleteLikeData(
        destroyed
    )
}