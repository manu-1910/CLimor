package io.square1.limor.remote.mappers

import entities.request.DataCommentRequest
import entities.request.DataCreateCommentRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCommentRequest
import io.square1.limor.remote.entities.requests.NWCreateCommentRequest

fun Single<NWCreateCommentRequest>.asDataEntity(): Single<DataCreateCommentRequest> {
    return this.map { it.asDataEntity() }
}


fun NWCreateCommentRequest.asDataEntity(): DataCreateCommentRequest {
    return DataCreateCommentRequest(
        comment.asDataEntity()
    )
}

fun NWCommentRequest.asDataEntity(): DataCommentRequest {
    return DataCommentRequest(
        content, duration, audio_url
    )
}

fun DataCreateCommentRequest.asRemoteEntity(): NWCreateCommentRequest {
    return NWCreateCommentRequest(
        comment.asRemoteEntity()
    )
}

fun DataCommentRequest.asRemoteEntity(): NWCommentRequest {
    return NWCommentRequest(
        content, duration, audio_url
    )
}


