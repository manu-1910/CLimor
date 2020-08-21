package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<CreateFriendResponseEntity>.asUIModel(): Single<UICreateFriendResponse> {
    return this.map { it.asUIModel() }
}


fun CreateFriendResponseEntity.asUIModel(): UICreateFriendResponse {
    return UICreateFriendResponse(
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