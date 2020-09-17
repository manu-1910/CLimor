package io.square1.limor.remote.mappers

import entities.request.DataUserIDRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWUserIDRequest


fun Single<NWUserIDRequest>.asDataEntity(): Single<DataUserIDRequest> {
    return this.map { it.asDataEntity() }
}


fun NWUserIDRequest.asDataEntity(): DataUserIDRequest {
    return DataUserIDRequest(
        user_id
    )
}


fun DataUserIDRequest.asRemoteEntity() : NWUserIDRequest {
    return NWUserIDRequest(
        user_id
    )
}

