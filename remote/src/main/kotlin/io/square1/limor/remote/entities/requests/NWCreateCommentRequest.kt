package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWCreateCommentRequest(
    var comment: NWCommentRequest = NWCommentRequest()
)

@Serializable
data class NWCommentRequest(
    var content: String = "",
    @Optional
    var duration: Int? = 0,
    @Optional
    var audio_url: String? = ""
)