package io.square1.limor.remote.mappers

import entities.request.DataLogoutRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWLogoutRequest


fun Single<NWLogoutRequest>.asDataEntity(): Single<DataLogoutRequest> {
    return this.map { it.asDataEntity() }
}


fun NWLogoutRequest.asDataEntity(): DataLogoutRequest {
    return DataLogoutRequest(
        token,
        uuid
    )
}


fun Single<DataLogoutRequest>.asRemoteEntity(): Single<NWLogoutRequest> {
    return this.map { it.asRemoteEntity() }
}


fun DataLogoutRequest.asRemoteEntity() : NWLogoutRequest {
    return NWLogoutRequest(
        token,
        uuid
    )
}
