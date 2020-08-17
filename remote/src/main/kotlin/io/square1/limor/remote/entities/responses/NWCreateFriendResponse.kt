package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWCreateFriendResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWFollowed? = NWFollowed()
)

@Serializable
data class NWFollowed (
    val followed: Boolean = true
)