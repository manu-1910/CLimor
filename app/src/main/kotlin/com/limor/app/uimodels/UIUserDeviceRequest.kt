package com.limor.app.uimodels


data class UIUserDeviceRequest(
    var device: UIUserDeviceData
)

data class UIUserDeviceData(
    var uuid: String,
    var platform: String,
    var push_token: String
)