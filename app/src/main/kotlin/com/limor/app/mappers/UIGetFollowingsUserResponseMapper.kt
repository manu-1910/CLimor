package com.limor.app.mappers

import com.limor.app.uimodels.*
import entities.response.GetFollowingsUsersDataEntity
import entities.response.GetFollowingsUsersResponseEntity
import io.reactivex.Single

fun Single<GetFollowingsUsersResponseEntity>.asUIModel(): Single<UIGetFollowingsUsersResponse> {
    return this.map { it.asUIModel() }
}

fun GetFollowingsUsersResponseEntity.asUIModel(): UIGetFollowingsUsersResponse {
    return UIGetFollowingsUsersResponse(
        code, message, data.asUIModel()
    )
}

fun GetFollowingsUsersDataEntity.asUIModel(): UIFollowingsUsersDataArray {
    return UIFollowingsUsersDataArray(
        getAllUIUsers(followed_users)
    )
}

