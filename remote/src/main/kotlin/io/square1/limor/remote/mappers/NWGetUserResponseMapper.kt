package io.square1.limor.remote.mappers

import entities.response.GetUserEntity
import entities.response.GetUserResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWGetUserData
import io.square1.limor.remote.entities.responses.NWGetUserResponse


fun Single<NWGetUserResponse>.asDataEntity(): Single<GetUserResponseEntity> {
    return this.map { it.asDataEntity() }
}

fun NWGetUserResponse.asDataEntity(): GetUserResponseEntity {
    return GetUserResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWGetUserData.asDataEntity() : GetUserEntity {
    return GetUserEntity(user.asDataEntity())
}

