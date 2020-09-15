package io.square1.limor.mappers

import entities.response.CreateCommentReportResponseEntity
import entities.response.ReportedEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UICreateCommentReportResponse
import io.square1.limor.uimodels.UIReported


fun Single<CreateCommentReportResponseEntity>.asUIModel(): Single<UICreateCommentReportResponse> {
    return this.map { it.asUIModel() }
}


fun CreateCommentReportResponseEntity.asUIModel(): UICreateCommentReportResponse {
    return UICreateCommentReportResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun ReportedEntity.asUIModel(): UIReported {
    return UIReported(
        reported
    )
}