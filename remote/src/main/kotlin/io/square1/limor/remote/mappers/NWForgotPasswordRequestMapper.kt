package io.square1.limor.remote.mappers

import entities.request.DataForgotPasswordRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWForgotPasswordRequest


fun Single<NWForgotPasswordRequest>.asDataEntity(): Single<DataForgotPasswordRequest> {
    return this.map { it.asDataEntity() }
}


fun NWForgotPasswordRequest.asDataEntity(): DataForgotPasswordRequest {
    return DataForgotPasswordRequest(
        email
    )
}


fun DataForgotPasswordRequest.asRemoteEntity() : NWForgotPasswordRequest {
    return NWForgotPasswordRequest(
        email
    )
}

