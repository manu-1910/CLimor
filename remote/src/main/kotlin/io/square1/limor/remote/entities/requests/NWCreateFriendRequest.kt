package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWCreateFriendRequest(
    var user_id: Int = 0
)
