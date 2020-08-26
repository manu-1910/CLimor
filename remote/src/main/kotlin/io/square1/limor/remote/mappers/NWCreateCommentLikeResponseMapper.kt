package io.square1.limor.remote.mappers

import entities.response.CommentLikeEntity
import entities.response.CreateCommentLikeData
import entities.response.CreateCommentLikeResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCommentCreateLikeData
import io.square1.limor.remote.entities.responses.NWCommentLike
import io.square1.limor.remote.entities.responses.NWCreateCommentLikeResponse


fun Single<NWCreateCommentLikeResponse>.asDataEntity(): Single<CreateCommentLikeResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWCreateCommentLikeResponse.asDataEntity(): CreateCommentLikeResponseEntity {
    return CreateCommentLikeResponseEntity(
        code,
        message,
        data?.asDataEntity()
    )
}


fun CreateCommentLikeResponseEntity.asRemoteEntity(): NWCreateCommentLikeResponse {
    return NWCreateCommentLikeResponse(
        code,
        message,
        data?.asRemoteEntity()
    )
}

fun NWCommentCreateLikeData.asDataEntity(): CreateCommentLikeData {
    return CreateCommentLikeData(
        like?.asDataEntity()
    )
}

fun NWCommentLike.asDataEntity(): CommentLikeEntity {
    return CommentLikeEntity(
        comment_id,
        user_id
    )
}


fun CreateCommentLikeData.asRemoteEntity(): NWCommentCreateLikeData {
    return NWCommentCreateLikeData(
        like?.asRemoteEntity()
    )
}

fun CommentLikeEntity.asRemoteEntity(): NWCommentLike {
    return NWCommentLike(
        comment_id,
        user_id
    )
}