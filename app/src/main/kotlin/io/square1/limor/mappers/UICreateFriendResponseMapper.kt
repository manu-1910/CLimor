package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


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