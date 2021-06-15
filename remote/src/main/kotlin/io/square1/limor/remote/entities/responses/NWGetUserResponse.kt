package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWGetUserResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWGetUserData = NWGetUserData()
)

@Serializable
data class NWGetUserData(

    val user: NWUser = NWUser()
)
