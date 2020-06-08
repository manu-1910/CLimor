package io.square1.limor.remote.entities.requests


import kotlinx.serialization.Serializable

@Serializable
data class NWLogoutRequest(
    val token: String,
    val uuid: String
)