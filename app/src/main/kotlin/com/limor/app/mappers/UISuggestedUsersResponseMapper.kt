package com.limor.app.mappers

import com.limor.app.uimodels.UISuggestedUsersResponse
import com.limor.app.uimodels.UIUser
import com.limor.app.uimodels.UIUsersArray
import entities.response.SuggestedUsersResponseEntity
import entities.response.UserEntity
import entities.response.UsersArrayEntity
import io.reactivex.Single


fun Single<SuggestedUsersResponseEntity>.asUIModel(): Single<UISuggestedUsersResponse> {
    return this.map { it.asUIModel() }
}

fun SuggestedUsersResponseEntity.asUIModel(): UISuggestedUsersResponse {
    return UISuggestedUsersResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun UsersArrayEntity.asUIModel() : UIUsersArray {
    return UIUsersArray(
        getSuggestedUsers(users)
    )
}

fun getSuggestedUsers(entityList: ArrayList<UserEntity>?): ArrayList<UIUser> {
    val userList = ArrayList<UIUser>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                userList.add(item.asUIModel())
        }
    }
    return userList
}