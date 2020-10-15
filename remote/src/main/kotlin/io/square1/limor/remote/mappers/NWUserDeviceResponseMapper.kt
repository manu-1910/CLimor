package io.square1.limor.remote.mappers


import entities.response.DeviceEntity
import entities.response.UserDeviceResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWDevice
import io.square1.limor.remote.entities.responses.NWUserDeviceResponse


fun Single<NWUserDeviceResponse>.asDataEntity(): Single<UserDeviceResponseEntity>? {
    return this.map { it.asDataEntity() }
}


fun NWUserDeviceResponse.asDataEntity(): UserDeviceResponseEntity{
    return UserDeviceResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun NWDevice.asDataEntity(): DeviceEntity{
    return DeviceEntity(
        id,
        platform,
        uuid,
        push_token,
        endpoint_arn,
        active
    )
}
