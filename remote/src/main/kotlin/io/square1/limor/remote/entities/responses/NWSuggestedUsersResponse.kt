package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWSuggestedUsersResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWUsersArray = NWUsersArray()
)

@Serializable
data class NWUsersArray(

    val users: ArrayList<NWUser> = ArrayList()
)