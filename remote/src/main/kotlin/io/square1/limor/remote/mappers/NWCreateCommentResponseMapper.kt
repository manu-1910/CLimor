package io.square1.limor.remote.mappers

import entities.response.CreateCommentData
import entities.response.CreateCommentResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCreateCommentData
import io.square1.limor.remote.entities.responses.NWCreateCommentResponse

fun Single<NWCreateCommentResponse>.asDataEntity(): Single<CreateCommentResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateCommentResponse.asDataEntity(): CreateCommentResponseEntity {
    return CreateCommentResponseEntity(
        code, message, data?.asDataEntity()
    )
}


fun NWCreateCommentData.asDataEntity(): CreateCommentData {
    return CreateCommentData(
        comment.asDataEntity()
    )
}

