package io.square1.limor.remote.mappers


import entities.request.*
import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.*
import io.square1.limor.remote.entities.responses.*


fun Single<NWUpdateProfileRequest>.asDataEntity(): Single<DataUpdateProfileRequest> {
    return this.map { it.asDataEntity() }
}


fun NWUpdateProfileRequest.asDataEntity(): DataUpdateProfileRequest {
    return DataUpdateProfileRequest(
        user.asDataEntity()
    )
}


fun DataUpdateProfileRequest.asRemoteEntity() : NWUpdateProfileRequest {
    return NWUpdateProfileRequest(
        user.asRemoteEntity()
    )
}



fun NWUpdateUser.asDataEntity(): UpdateUserEntity {
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


fun UpdateUserEntity.asRemoteEntity() : NWUpdateUser {
    return NWUpdateUser(
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






