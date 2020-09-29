package io.square1.limor.mappers

import entities.response.SuggestedUsersResponseEntity
import entities.response.UserEntity
import entities.response.UsersArrayEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UISuggestedUsersResponse
import io.square1.limor.uimodels.UIUser
import io.square1.limor.uimodels.UIUsersArray


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