package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWUserDeviceResponse(
    var code: Int,
    var message: String,
    var data: NWDevice
)

@Serializable
data class NWDevice(
    @Optional
    var id: Int = 0,
    @Optional
    var platform: String = "android",
    @Optional
    var uuid: String = "",
    @Optional
    var push_token: String = "",
    @Optional
    var endpoint_arn: String = "",
    @Optional
    var active: Boolean = false
)