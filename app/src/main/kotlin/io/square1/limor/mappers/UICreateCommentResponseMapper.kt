package io.square1.limor.mappers

import entities.response.CreateCommentData
import entities.response.CreateCommentResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UICreateCommentResponse
import io.square1.limor.uimodels.UIDataCreateComment


fun Single<CreateCommentResponseEntity>.asUIModel(): Single<UICreateCommentResponse> {
    return this.map { it.asUIModel() }
}

fun CreateCommentResponseEntity.asUIModel(): UICreateCommentResponse {
    return UICreateCommentResponse(
        code, message, data?.asUIModel()
    )
}

fun CreateCommentData.asUIModel(): UIDataCreateComment {
    return UIDataCreateComment(
        comment.asUIModel()
    )
}