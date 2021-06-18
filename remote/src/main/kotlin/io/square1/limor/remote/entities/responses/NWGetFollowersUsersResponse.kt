package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWGetFollowersUsersResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWFollowersUsersData = NWFollowersUsersData()
)

@Serializable
data class NWFollowersUsersData(

    val following_users : ArrayList<NWUser> = ArrayList()
)
