package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWCreateCommentResponse (
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWCreateCommentData? = NWCreateCommentData()
)



@Serializable
data class NWCreateCommentData (
    @Optional
    val comment: NWComment = NWComment()
)


