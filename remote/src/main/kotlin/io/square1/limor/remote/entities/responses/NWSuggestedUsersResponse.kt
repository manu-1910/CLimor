package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWSuggestedUsersResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWUsersArray = NWUsersArray()
)

@Serializable
data class NWUsersArray(
    @Optional
    val users: ArrayList<NWUser> = ArrayList()
)