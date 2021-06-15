package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWGetBlockedUsersResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWBlockedUsersData = NWBlockedUsersData()
)

@Serializable
data class NWBlockedUsersData(

    val blocked_users : ArrayList<NWUser> = ArrayList()
)
