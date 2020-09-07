package io.square1.limor.mappers

import entities.request.DataCommentRequest
import entities.request.DataCreateCommentRequest
import io.reactivex.Single
import io.square1.limor.uimodels.UICommentRequest
import io.square1.limor.uimodels.UICreateCommentRequest

fun Single<DataCreateCommentRequest>.asUIModel(): Single<UICreateCommentRequest> {
    return this.map { it.asUIModel() }
}


fun DataCreateCommentRequest.asUIModel(): UICreateCommentRequest {
    return UICreateCommentRequest(
        comment.asUIModel()
    )
}

fun DataCommentRequest.asUIModel(): UICommentRequest {
    return UICommentRequest(
        content, duration, audio_url
    )
}

fun UICommentRequest.asDataEntity() : DataCommentRequest {
    return DataCommentRequest(
        content, duration, audio_url
    )
}

fun UICreateCommentRequest.asDataEntity(): DataCreateCommentRequest {
    return DataCreateCommentRequest(
        comment.asDataEntity()
    )
}
