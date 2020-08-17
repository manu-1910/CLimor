package io.square1.limor.remote.mappers

import entities.response.CreateFriendResponseEntity
import entities.response.FollowedEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCreateFriendResponse
import io.square1.limor.remote.entities.responses.NWFollowed


fun Single<NWCreateFriendResponse>.asDataEntity(): Single<CreateFriendResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateFriendResponse.asDataEntity(): CreateFriendResponseEntity {
    return CreateFriendResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun CreateFriendResponseEntity.asRemoteEntity(): NWCreateFriendResponse {
    return NWCreateFriendResponse(
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