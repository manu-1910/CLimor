package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWGetFollowingsUsersResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWFollowingsUsersData = NWFollowingsUsersData()
)

@Serializable
data class NWFollowingsUsersData(

    val followed_users : ArrayList<NWUser> = ArrayList()
)
