package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWBlockedUserResponse (

    val code: Int = 0,

    val message: String = "",

    val data: NWBlockedUserData = NWBlockedUserData()
)

@Serializable
data class NWBlockedUserData(

    val blocked: Boolean = false
)