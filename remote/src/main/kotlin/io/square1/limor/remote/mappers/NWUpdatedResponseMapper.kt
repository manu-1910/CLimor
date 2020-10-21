package io.square1.limor.remote.mappers

import entities.response.UpdatedData
import entities.response.UpdatedResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWUpdatedData
import io.square1.limor.remote.entities.responses.NWUpdatedResponse

fun Single<NWUpdatedResponse>.asDataEntity(): Single<UpdatedResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWUpdatedResponse.asDataEntity(): UpdatedResponseEntity {
    return UpdatedResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun UpdatedResponseEntity.asRemoteEntity(): NWUpdatedResponse {
    return NWUpdatedResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWUpdatedData.asDataEntity(): UpdatedData {
    return UpdatedData(
        updated
    )
}


fun UpdatedData.asRemoteEntity(): NWUpdatedData {
    return NWUpdatedData(
        updated
    )
}