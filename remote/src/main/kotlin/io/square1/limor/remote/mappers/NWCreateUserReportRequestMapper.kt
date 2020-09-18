package io.square1.limor.remote.mappers

import entities.request.DataCreateUserReportRequestEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateUserReportRequest


fun Single<NWCreateUserReportRequest>.asDataEntity(): Single<DataCreateUserReportRequestEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateUserReportRequest.asDataEntity(): DataCreateUserReportRequestEntity {
    return DataCreateUserReportRequestEntity(
        reason
    )
}


fun DataCreateUserReportRequestEntity.asRemoteEntity() : NWCreateUserReportRequest {
    return NWCreateUserReportRequest(
        reason
    )
}

