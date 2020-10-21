package com.limor.app.mappers

import com.limor.app.uimodels.UICommentLike
import com.limor.app.uimodels.UICreateCommentLikeData
import com.limor.app.uimodels.UICreateCommentLikeResponse
import entities.response.CommentLikeEntity
import entities.response.CreateCommentLikeData
import entities.response.CreateCommentLikeResponseEntity
import io.reactivex.Single


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