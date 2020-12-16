package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWGetFollowingsUsersResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWFollowingsUsersData = NWFollowingsUsersData()
)

@Serializable
data class NWFollowingsUsersData(
    @Optional
    val followed_users : ArrayList<NWUser> = ArrayList()
)
