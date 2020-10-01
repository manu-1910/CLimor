package io.square1.limor.mappers


import entities.request.DataUpdateProfileRequest
import entities.request.UpdateUserEntity
import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun UIUpdateProfileRequest.asDataEntity(): DataUpdateProfileRequest {
    return DataUpdateProfileRequest(
        user.asDataEntity()
    )
}

fun Single<UIUpdateProfileRequest>.asDataEntity(): Single<DataUpdateProfileRequest> {
    return this.map { it.asDataEntity() }
}

fun UIUpdateUser.asDataEntity(): UpdateUserEntity {
    return UpdateUserEntity(
        first_name,
        last_name,
        username,
        website,
        description,
        email,
        phone_number,
        date_of_birth,
        gender,
        image_url
    )
}


