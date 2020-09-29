package io.square1.limor.remote.mappers


import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWChangePasswordResponse>.asDataEntity(): Single<ChangePasswordResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWChangePasswordResponse.asDataEntity(): ChangePasswordResponseEntity{
    return ChangePasswordResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


