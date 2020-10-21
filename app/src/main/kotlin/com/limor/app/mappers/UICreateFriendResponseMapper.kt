package com.limor.app.mappers

import com.limor.app.uimodels.UICreateDeleteFriendResponse
import com.limor.app.uimodels.UIFollowed
import entities.response.CreateDeleteFriendResponseEntity
import entities.response.FollowedEntity
import io.reactivex.Single


fun Single<CreateDeleteFriendResponseEntity>.asUIModel(): Single<UICreateDeleteFriendResponse> {
    return this.map { it.asUIModel() }
}


fun CreateDeleteFriendResponseEntity.asUIModel(): UICreateDeleteFriendResponse {
    return UICreateDeleteFriendResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun FollowedEntity.asUIModel(): UIFollowed {
    return UIFollowed(
        followed
    )
}