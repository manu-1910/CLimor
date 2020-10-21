package com.limor.app.mappers

import com.limor.app.uimodels.UICommentRequest
import com.limor.app.uimodels.UICreateCommentRequest
import entities.request.DataCommentRequest
import entities.request.DataCreateCommentRequest
import io.reactivex.Single

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
