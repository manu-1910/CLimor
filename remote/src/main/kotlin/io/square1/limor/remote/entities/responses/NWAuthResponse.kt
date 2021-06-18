package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable


@Serializable
data class NWAuthResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWDataAuthResponse = NWDataAuthResponse()

)
@Serializable
data class NWDataAuthResponse(

    val token: NWToken = NWToken()
)
@Serializable
data class NWToken(

    val access_token: String = "",

    val token_type: String = "",

    val expires_in: Long = 0,

    val scope: String = "",

    val created_at: Long = 0
)