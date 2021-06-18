package io.square1.limor.remote.entities.requests


import kotlinx.serialization.Serializable


@Serializable
data class NWCreateCommentRequest(
    var comment: NWCommentRequest = NWCommentRequest()
)

@Serializable
data class NWCommentRequest(
    var content: String = "",

    var duration: Int? = 0,

    var audio_url: String? = ""
)