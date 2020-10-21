package com.limor.app.uimodels

data class UIUserDeviceResponse(
    var code: Int,
    var message: String,
    var data: UIDevice
)

data class UIDevice(
    var id: Int,
    var platform: String,
    var uuid: String,
    var push_token: String,
    var endpoint_arn: String,
    var active: Boolean
)

