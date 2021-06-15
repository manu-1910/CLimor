package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWCreateCommentResponse (

    val code: Int = 0,

    val message: String = "",

    val data: NWCreateCommentData? = NWCreateCommentData()
)



@Serializable
data class NWCreateCommentData (

    val comment: NWComment = NWComment()
)


