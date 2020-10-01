package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWGetUserResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWGetUserData = NWGetUserData()
)

@Serializable
data class NWGetUserData(
    @Optional
    val user: NWUser = NWUser()
)
