package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWFollowingsUsersData
import io.square1.limor.remote.entities.responses.NWGetFollowingsUsersResponse


fun Single<NWGetFollowingsUsersResponse>.asDataEntity(): Single<GetFollowingsUsersResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWGetFollowingsUsersResponse.asDataEntity(): GetFollowingsUsersResponseEntity {
    return GetFollowingsUsersResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun NWFollowingsUsersData.asDataEntity(): GetFollowingsUsersDataEntity {
    return GetFollowingsUsersDataEntity(
        getAllUsersEntities(followed_users)
    )
}



