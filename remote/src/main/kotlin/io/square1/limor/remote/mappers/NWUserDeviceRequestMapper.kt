package io.square1.limor.remote.mappers


import entities.request.*
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.*


//***** FROM REMOTE TO DATA
fun Single<NWUserDeviceRequest>.asDataEntity(): Single<DataUserDeviceRequest> {
    return this.map { it.asDataEntity() }
}

fun NWUserDeviceRequest.asDataEntity(): DataUserDeviceRequest {
    return DataUserDeviceRequest(
        device.asDataEntity()
    )
}

fun NWUserDeviceData.asDataEntity(): DataUserDeviceData {
    return DataUserDeviceData(
        uuid,
        platform,
        push_token
    )
}


//***** FROM DATA TO REMOTE
fun Single<DataUserDeviceRequest>.asRemoteEntity(): Single<NWUserDeviceRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataUserDeviceRequest.asRemoteEntity() : NWUserDeviceRequest {
    return NWUserDeviceRequest(
        device.asRemoteEntity()
    )
}


fun DataUserDeviceData.asRemoteEntity(): NWUserDeviceData {
    return NWUserDeviceData(
        uuid,
        platform,
        push_token
    )
}

