package io.square1.limor.remote.mappers

import entities.request.DataChangePasswordRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWChangePasswordRequest


fun Single<NWChangePasswordRequest>.asDataEntity(): Single<DataChangePasswordRequest> {
    return this.map { it.asDataEntity() }
}


fun NWChangePasswordRequest.asDataEntity(): DataChangePasswordRequest {
    return DataChangePasswordRequest(
        current_password, new_password
    )
}


fun Single<DataChangePasswordRequest>.asRemoteEntity(): Single<NWChangePasswordRequest> {
    return this.map { it.asRemoteEntity() }
}


fun DataChangePasswordRequest.asRemoteEntity() : NWChangePasswordRequest {
    return NWChangePasswordRequest(
        current_password, new_password
    )
}

