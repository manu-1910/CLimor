package io.square1.limor.remote.entities.requests


import kotlinx.serialization.Serializable

@Serializable
data class NWUserDeviceRequest(
    var device: NWUserDeviceData
)

@Serializable
data class NWUserDeviceData(
    var uuid: String = "",
    var platform: String = "",
    var push_token: String = ""
)