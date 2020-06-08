package io.square1.limor.uimodels

data class UIAuthResponse(
    val code: Int = 0,
    val message: String = "",
    val data: UIDataAuthResponse
)

data class UIDataAuthResponse(
    val token: UIToken
)

data class UIToken(
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val scope: String,
    val created_at: Long
)
