package io.square1.limor.remote.mappers

import entities.response.CreateReportResponseEntity
import entities.response.ReportedEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCreateReportResponse
import io.square1.limor.remote.entities.responses.NWReported


fun Single<NWCreateReportResponse>.asDataEntity(): Single<CreateReportResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateReportResponse.asDataEntity(): CreateReportResponseEntity {
    return CreateReportResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun CreateReportResponseEntity.asRemoteEntity(): NWCreateReportResponse {
    return NWCreateReportResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWReported.asDataEntity(): ReportedEntity {
    return ReportedEntity(
        reported
    )
}


fun ReportedEntity.asRemoteEntity(): NWReported {
    return NWReported(
        reported
    )
}