package io.square1.limor.remote.mappers

import entities.request.DataContentRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWContentRequest


fun Single<NWContentRequest>.asDataEntity(): Single<DataContentRequest> {
    return this.map { it.asDataEntity() }
}


fun NWContentRequest.asDataEntity(): DataContentRequest {
    return DataContentRequest(
        content
    )
}


fun DataContentRequest.asRemoteEntity(): NWContentRequest {
    return NWContentRequest(
        content
    )
}

