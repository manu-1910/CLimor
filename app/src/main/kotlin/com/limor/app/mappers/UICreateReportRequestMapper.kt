package com.limor.app.mappers

import com.limor.app.uimodels.UICreateReportRequest
import entities.request.DataCreateReportRequestEntity
import io.reactivex.Single


fun Single<DataCreateReportRequestEntity>.asUIModel(): Single<UICreateReportRequest> {
    return this.map { it.asUIModel() }
}


fun DataCreateReportRequestEntity.asUIModel(): UICreateReportRequest {
    return UICreateReportRequest(
        reason
    )
}

fun UICreateReportRequest.asDataEntity() : DataCreateReportRequestEntity {
    return DataCreateReportRequestEntity(
        reason
    )
}