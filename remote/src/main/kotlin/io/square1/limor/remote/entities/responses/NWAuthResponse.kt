package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWAuthResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWDataAuthResponse = NWDataAuthResponse()

)
@Serializable
data class NWDataAuthResponse(
    @Optional
    val token: NWToken = NWToken()
)
@Serializable
data class NWToken(
    @Optional
    val access_token: String = "",
    @Optional
    val token_type: String = "",
    @Optional
    val expires_in: Long = 0,
    @Optional
    val scope: String = "",
    @Optional
    val created_at: Long = 0
)