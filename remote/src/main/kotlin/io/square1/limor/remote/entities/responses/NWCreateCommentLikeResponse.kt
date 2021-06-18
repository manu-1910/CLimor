package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWCreateCommentLikeResponse (

    val code: Int = 0,

    val message: String = "",

    val data: NWCommentCreateLikeData? = NWCommentCreateLikeData()
)

@Serializable
data class NWCommentCreateLikeData(

    val like: NWCommentLike? = NWCommentLike()
)

@Serializable
data class NWCommentLike (

    val comment_id : Int = 0,

    val user_id : Int = 0
)