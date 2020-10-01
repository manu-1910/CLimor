package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWBlockedUserResponse (
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWBlockedUserData = NWBlockedUserData()
)

@Serializable
data class NWBlockedUserData(
    @Optional
    val blocked: Boolean = false
)