package io.square1.limor.remote.entities.requests

import entities.response.ImagesEntity
import io.square1.limor.remote.entities.responses.NWUser
import kotlinx.serialization.Serializable


@Serializable
data class NWUpdateProfileRequest(
    var user: NWUpdateUser
)

@Serializable
data class NWUpdateUser(
    var first_name: String?,
    var last_name: String?,
    var username: String?,
    var website: String?,
    var description: String?,
    var email: String?,
    var phone_number: String?,
    var date_of_birth: Long?,
    var gender: String?,
    var notifications_enabled: Boolean?,
    var image_url: String?
)
