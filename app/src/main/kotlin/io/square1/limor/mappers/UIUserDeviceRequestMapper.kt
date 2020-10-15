package io.square1.limor.mappers

import entities.request.*
import io.square1.limor.uimodels.*


//****** FROM UI TO DATA
fun UIUserDeviceRequest.asDataEntity(): DataUserDeviceRequest {
    return DataUserDeviceRequest(
        device.asDataEntity()
    )
}

fun UIUserDeviceData.asDataEntity(): DataUserDeviceData {
    return DataUserDeviceData(
        uuid,
        platform,
        push_token
    )
}


//****** FROM DATA TO UI
fun DataUserDeviceRequest.asUIModel(): UIUserDeviceRequest {
    return UIUserDeviceRequest(
        device.asUIModel()
    )
}

fun DataUserDeviceData.asUIModel(): UIUserDeviceData {
    return UIUserDeviceData(
        uuid,
        platform,
        push_token
    )
}
