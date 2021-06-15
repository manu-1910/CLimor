package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWCreateDeleteFriendResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWFollowed? = NWFollowed()
)

@Serializable
data class NWFollowed (
    val followed: Boolean = true
)