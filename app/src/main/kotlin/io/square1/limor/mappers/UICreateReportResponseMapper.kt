package io.square1.limor.mappers

import entities.response.CreateReportResponseEntity
import entities.response.ReportedEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UICreateReportResponse
import io.square1.limor.uimodels.UIReported


fun Single<CreateReportResponseEntity>.asUIModel(): Single<UICreateReportResponse> {
    return this.map { it.asUIModel() }
}


fun CreateReportResponseEntity.asUIModel(): UICreateReportResponse {
    return UICreateReportResponse(
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