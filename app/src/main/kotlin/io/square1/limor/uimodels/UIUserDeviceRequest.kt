package io.square1.limor.uimodels


data class UIUserDeviceRequest(
    var device: UIUserDeviceData
)

data class UIUserDeviceData(
    var uuid: String,
    var platform: String,
    var push_token: String
)