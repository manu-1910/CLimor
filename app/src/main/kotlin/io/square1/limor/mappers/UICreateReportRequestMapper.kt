package io.square1.limor.mappers

import entities.request.DataCreateReportRequestEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UICreateReportRequest


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