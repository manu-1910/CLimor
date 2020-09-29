package io.square1.limor.remote.mappers

import entities.request.DataCreateReportRequestEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateReportRequest


fun Single<NWCreateReportRequest>.asDataEntity(): Single<DataCreateReportRequestEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateReportRequest.asDataEntity(): DataCreateReportRequestEntity {
    return DataCreateReportRequestEntity(
        reason
    )
}


fun DataCreateReportRequestEntity.asRemoteEntity() : NWCreateReportRequest {
    return NWCreateReportRequest(
        reason
    )
}

