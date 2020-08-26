package io.square1.limor.mappers

import entities.response.CommentLikeEntity
import entities.response.CreateCommentLikeData
import entities.response.CreateCommentLikeResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UICommentLike
import io.square1.limor.uimodels.UICreateCommentLikeData
import io.square1.limor.uimodels.UICreateCommentLikeResponse


fun Single<CreateCommentLikeResponseEntity>.asUIModel(): Single<UICreateCommentLikeResponse> {
    return this.map { it.asUIModel() }
}


fun CreateCommentLikeResponseEntity.asUIModel(): UICreateCommentLikeResponse {
    return UICreateCommentLikeResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun CreateCommentLikeData.asUIModel(): UICreateCommentLikeData {
    return UICreateCommentLikeData(
        like?.asUIModel()
    )
}

fun CommentLikeEntity.asUIModel(): UICommentLike {
    return UICommentLike(comment_id, user_id)
}