package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*

fun Single<NWDeleteResponse>.asDataEntity(): Single<DeleteResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWDeleteResponse.asDataEntity(): DeleteResponseEntity {
    return DeleteResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun DeleteResponseEntity.asRemoteEntity(): NWDeleteResponse {
    return NWDeleteResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWDeleteData.asDataEntity(): DeleteData {
    return DeleteData(
        destroyed
    )
}


fun DeleteData.asRemoteEntity(): NWDeleteData {
    return NWDeleteData(
        destroyed
    )
}