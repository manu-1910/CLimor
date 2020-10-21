package com.limor.app.mappers

import com.limor.app.uimodels.UIUserDeviceData
import com.limor.app.uimodels.UIUserDeviceRequest
import entities.request.DataUserDeviceData
import entities.request.DataUserDeviceRequest


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
