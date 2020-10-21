package com.limor.app.mappers

import com.limor.app.uimodels.UICreateReportResponse
import com.limor.app.uimodels.UIReported
import entities.response.CreateReportResponseEntity
import entities.response.ReportedEntity
import io.reactivex.Single


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