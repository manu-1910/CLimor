package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWSuggestedUsersResponse>.asDataEntity(): Single<SuggestedUsersResponseEntity> {
    return this.map { it.asDataEntity() }
}

fun NWSuggestedUsersResponse.asDataEntity(): SuggestedUsersResponseEntity {
    return SuggestedUsersResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWUsersArray.asDataEntity() : UsersArrayEntity {
    return UsersArrayEntity(
        getSuggestedUserEntities(users)
    )
}

fun getSuggestedUserEntities(nwList: ArrayList<NWUser>?): ArrayList<UserEntity> {
    val entityList = ArrayList<UserEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}
