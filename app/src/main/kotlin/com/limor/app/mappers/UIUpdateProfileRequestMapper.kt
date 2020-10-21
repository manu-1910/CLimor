package com.limor.app.mappers


import com.limor.app.uimodels.UIUpdateProfileRequest
import com.limor.app.uimodels.UIUpdateUser
import entities.request.DataUpdateProfileRequest
import entities.request.UpdateUserEntity
import io.reactivex.Single



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
        notifications_enabled,
        image_url
    )
}


