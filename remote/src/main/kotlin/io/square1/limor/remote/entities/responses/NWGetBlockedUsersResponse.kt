package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWGetBlockedUsersResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWBlockedUsersData = NWBlockedUsersData()
)

@Serializable
data class NWBlockedUsersData(
    @Optional
    val blocked_users : ArrayList<NWUser> = ArrayList()
)
