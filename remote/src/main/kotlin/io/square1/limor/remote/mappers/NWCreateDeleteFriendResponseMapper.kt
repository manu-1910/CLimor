package io.square1.limor.remote.mappers

import entities.response.CreateDeleteFriendResponseEntity
import entities.response.FollowedEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCreateDeleteFriendResponse
import io.square1.limor.remote.entities.responses.NWFollowed


fun Single<NWCreateDeleteFriendResponse>.asDataEntity(): Single<CreateDeleteFriendResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateDeleteFriendResponse.asDataEntity(): CreateDeleteFriendResponseEntity {
    return CreateDeleteFriendResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun CreateDeleteFriendResponseEntity.asRemoteEntity(): NWCreateDeleteFriendResponse {
    return NWCreateDeleteFriendResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWFollowed.asDataEntity(): FollowedEntity {
    return FollowedEntity(
        followed
    )
}




fun FollowedEntity.asRemoteEntity() : NWFollowed{
    return NWFollowed(
        followed
    )
}