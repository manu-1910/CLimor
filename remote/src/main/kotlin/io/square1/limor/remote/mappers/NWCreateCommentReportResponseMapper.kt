package io.square1.limor.remote.mappers

import entities.response.CreateCommentReportResponseEntity
import entities.response.ReportedEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCreateCommentReportResponse
import io.square1.limor.remote.entities.responses.NWReported


fun Single<NWCreateCommentReportResponse>.asDataEntity(): Single<CreateCommentReportResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateCommentReportResponse.asDataEntity(): CreateCommentReportResponseEntity {
    return CreateCommentReportResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun CreateCommentReportResponseEntity.asRemoteEntity(): NWCreateCommentReportResponse {
    return NWCreateCommentReportResponse(
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