package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWFollowersUsersData
import io.square1.limor.remote.entities.responses.NWGetFollowersUsersResponse


fun Single<NWGetFollowersUsersResponse>.asDataEntity(): Single<GetFollowersUsersResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWGetFollowersUsersResponse.asDataEntity(): GetFollowersUsersResponseEntity {
    return GetFollowersUsersResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun NWFollowersUsersData.asDataEntity(): GetFollowersUsersDataEntity {
    return GetFollowersUsersDataEntity(
        getAllUsersEntities(following_users)
    )
}



