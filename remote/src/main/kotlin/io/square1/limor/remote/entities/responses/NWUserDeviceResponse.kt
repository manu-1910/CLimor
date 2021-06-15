package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWUserDeviceResponse(
    var code: Int,
    var message: String,
    var data: NWDevice
)

@Serializable
data class NWDevice(

    var id: Int = 0,

    var platform: String = "android",

    var uuid: String = "",

    var push_token: String = "",

    var endpoint_arn: String = "",

    var active: Boolean = false
)