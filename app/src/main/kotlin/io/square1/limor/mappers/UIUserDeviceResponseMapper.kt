package io.square1.limor.mappers

import entities.response.DeviceEntity
import entities.response.UserDeviceResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIDevice
import io.square1.limor.uimodels.UIUserDeviceResponse


fun Single<UserDeviceResponseEntity>.asUIModel(): Single<UIUserDeviceResponse> {
    return this.map { it.asUIModel() }
}


fun UserDeviceResponseEntity.asUIModel(): UIUserDeviceResponse{
    return UIUserDeviceResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun DeviceEntity.asUIModel(): UIDevice{
    return UIDevice(
        id,
        platform,
        uuid,
        push_token,
        endpoint_arn,
        active
    )
}
