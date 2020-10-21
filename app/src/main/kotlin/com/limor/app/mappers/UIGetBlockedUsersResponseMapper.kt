package com.limor.app.mappers

import com.limor.app.uimodels.UIBlockedUsersDataArray
import com.limor.app.uimodels.UIGetBlockedUsersResponse
import com.limor.app.uimodels.UIUser
import entities.response.GetBlockedUsersDataEntity
import entities.response.GetBlockedUsersResponseEntity
import entities.response.UserEntity
import io.reactivex.Single

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
