package com.limor.app.mappers

import com.limor.app.uimodels.UICreateCommentResponse
import com.limor.app.uimodels.UIDataCreateComment
import entities.response.CreateCommentData
import entities.response.CreateCommentResponseEntity
import io.reactivex.Single


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