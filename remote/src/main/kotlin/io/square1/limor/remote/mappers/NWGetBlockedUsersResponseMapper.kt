package io.square1.limor.remote.mappers

import entities.response.GetBlockedUsersDataEntity
import entities.response.GetBlockedUsersResponseEntity
import entities.response.UserEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWBlockedUsersData
import io.square1.limor.remote.entities.responses.NWGetBlockedUsersResponse
import io.square1.limor.remote.entities.responses.NWUser

fun Single<NWGetBlockedUsersResponse>.asDataEntity(): Single<GetBlockedUsersResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWGetBlockedUsersResponse.asDataEntity(): GetBlockedUsersResponseEntity {
    return GetBlockedUsersResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun NWBlockedUsersData.asDataEntity(): GetBlockedUsersDataEntity {
    return GetBlockedUsersDataEntity(
        getAllUsersEntities(blocked_users)
    )
}


fun getAllUsersEntities(nwList: ArrayList<NWUser>?): ArrayList<UserEntity> {
    val entityList = ArrayList<UserEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}

