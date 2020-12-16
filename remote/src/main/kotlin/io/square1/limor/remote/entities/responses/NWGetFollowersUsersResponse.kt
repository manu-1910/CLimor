package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWGetFollowersUsersResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWFollowersUsersData = NWFollowersUsersData()
)

@Serializable
data class NWFollowersUsersData(
    @Optional
    val following_users : ArrayList<NWUser> = ArrayList()
)
