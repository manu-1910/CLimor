package io.square1.limor.remote.mappers

import entities.request.DataDropOffRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWDropOffRequest

fun Single<NWDropOffRequest>.asDataEntity(): Single<DataDropOffRequest> {
    return this.map { it.asDataEntity() }
}


fun NWDropOffRequest.asDataEntity(): DataDropOffRequest {
    return DataDropOffRequest(
        percentage
    )
}


fun DataDropOffRequest.asRemoteEntity() : NWDropOffRequest {
    return NWDropOffRequest(
        percentage
    )
}

