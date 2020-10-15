package entities.request


data class DataUserDeviceRequest(
    var device: DataUserDeviceData
)

data class DataUserDeviceData(
    var uuid: String,
    var platform: String,
    var push_token: String
)