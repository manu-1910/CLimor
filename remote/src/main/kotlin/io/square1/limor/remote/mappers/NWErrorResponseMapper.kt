package io.square1.limor.remote.mappers


import entities.response.ErrorResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWErrorResponse


fun Single<NWErrorResponse>.asDataEntity(): Single<ErrorResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWErrorResponse.asDataEntity(): ErrorResponseEntity {
    return ErrorResponseEntity(
        code,
        message
    )
}


fun Single<ErrorResponseEntity>.asRemoteEntity(): Single<NWErrorResponse> {
    return this.map { it.asRemoteEntity() }
}


fun ErrorResponseEntity.asRemoteEntity() : NWErrorResponse {
    return NWErrorResponse(
        code,
        message
    )
}
