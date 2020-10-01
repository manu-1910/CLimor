package io.square1.limor.remote.mappers

import entities.response.BlockedUserResponseEntity
import entities.response.UserBlockedEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWBlockedUserData
import io.square1.limor.remote.entities.responses.NWBlockedUserResponse


fun Single<NWBlockedUserResponse>.asDataEntity(): Single<BlockedUserResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWBlockedUserResponse.asDataEntity(): BlockedUserResponseEntity {
    return BlockedUserResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun BlockedUserResponseEntity.asRemoteEntity(): NWBlockedUserResponse {
    return NWBlockedUserResponse(
        code,
        message,
        data.asRemoteEntity()
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