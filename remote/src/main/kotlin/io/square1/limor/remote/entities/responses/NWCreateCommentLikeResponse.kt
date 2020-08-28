package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWCreateCommentLikeResponse (
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWCommentCreateLikeData? = NWCommentCreateLikeData()
)

@Serializable
data class NWCommentCreateLikeData(
    @Optional
    val like: NWCommentLike? = NWCommentLike()
)

@Serializable
data class NWCommentLike (
    @Optional
    val comment_id : Int = 0,
    @Optional
    val user_id : Int = 0
)