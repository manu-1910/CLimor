package io.square1.limor.mappers

import entities.request.DataCreateUserReportRequestEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UICreateUserReportRequest


fun Single<DataCreateUserReportRequestEntity>.asUIModel(): Single<UICreateUserReportRequest> {
    return this.map { it.asUIModel() }
}


fun DataCreateUserReportRequestEntity.asUIModel(): UICreateUserReportRequest {
    return UICreateUserReportRequest(
        reason
    )
}

fun UICreateUserReportRequest.asDataEntity() : DataCreateUserReportRequestEntity {
    return DataCreateUserReportRequestEntity(
        reason
    )
}