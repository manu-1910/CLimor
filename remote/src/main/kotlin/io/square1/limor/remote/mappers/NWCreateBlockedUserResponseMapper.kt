package io.square1.limor.remote.mappers

import entities.response.CreateBlockedUserResponseEntity
import entities.response.UserBlockedEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWBlockedUserData
import io.square1.limor.remote.entities.responses.NWCreateBlockedUserResponse


fun Single<NWCreateBlockedUserResponse>.asDataEntity(): Single<CreateBlockedUserResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateBlockedUserResponse.asDataEntity(): CreateBlockedUserResponseEntity {
    return CreateBlockedUserResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun CreateBlockedUserResponseEntity.asRemoteEntity(): NWCreateBlockedUserResponse {
    return NWCreateBlockedUserResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWBlockedUserData.asDataEntity(): UserBlockedEntity {
    return UserBlockedEntity(
        blocked
    )
}




fun UserBlockedEntity.asRemoteEntity() : NWBlockedUserData{
    return NWBlockedUserData(
        blocked
    )
}