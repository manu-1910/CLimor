package com.limor.app.mappers

import com.limor.app.uimodels.UIDevice
import com.limor.app.uimodels.UIUserDeviceResponse
import entities.response.DeviceEntity
import entities.response.UserDeviceResponseEntity
import io.reactivex.Single


fun Single<UserDeviceResponseEntity>.asUIModel(): Single<UIUserDeviceResponse> {
    return this.map { it.asUIModel() }
}


fun UserDeviceResponseEntity.asUIModel(): UIUserDeviceResponse {
    return UIUserDeviceResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun DeviceEntity.asUIModel(): UIDevice {
    return UIDevice(
        id,
        platform,
        uuid,
        push_token,
        endpoint_arn,
        active
    )
}
