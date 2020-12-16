package com.limor.app.mappers

import com.limor.app.uimodels.*
import entities.response.GetFollowersUsersDataEntity
import entities.response.GetFollowersUsersResponseEntity
import io.reactivex.Single

fun Single<GetFollowersUsersResponseEntity>.asUIModel(): Single<UIGetFollowersUsersResponse> {
    return this.map { it.asUIModel() }
}

fun GetFollowersUsersResponseEntity.asUIModel(): UIGetFollowersUsersResponse {
    return UIGetFollowersUsersResponse(
        code, message, data.asUIModel()
    )
}

fun GetFollowersUsersDataEntity.asUIModel(): UIFollowersUsersDataArray {
    return UIFollowersUsersDataArray(
        getAllUIUsers(following_users)
    )
}

