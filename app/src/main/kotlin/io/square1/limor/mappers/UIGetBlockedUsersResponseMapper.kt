package io.square1.limor.mappers

import entities.response.GetBlockedUsersDataEntity
import entities.response.GetBlockedUsersResponseEntity
import entities.response.UserEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIBlockedUsersDataArray
import io.square1.limor.uimodels.UIGetBlockedUsersResponse
import io.square1.limor.uimodels.UIUser

fun Single<GetBlockedUsersResponseEntity>.asUIModel(): Single<UIGetBlockedUsersResponse> {
    return this.map { it.asUIModel() }
}

fun GetBlockedUsersResponseEntity.asUIModel(): UIGetBlockedUsersResponse {
    return UIGetBlockedUsersResponse(
        code, message, data.asUIModel()
    )
}

fun GetBlockedUsersDataEntity.asUIModel(): UIBlockedUsersDataArray {
    return UIBlockedUsersDataArray(
        getAllUIUsers(blocked_users)
    )
}


fun getAllUIUsers(entityList: ArrayList<UserEntity>?): ArrayList<UIUser> {
    val uiList = ArrayList<UIUser>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}
