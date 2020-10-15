package entities.response

data class UserDeviceResponseEntity(
    var code: Int,
    var message: String,
    var data: DeviceEntity
)

data class DeviceEntity(
    var id: Int,
    var platform: String,
    var uuid: String,
    var push_token: String,
    var endpoint_arn: String,
    var active: Boolean
)

