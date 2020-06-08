package io.square1.limor.remote.mappers

import entities.request.DataMergeFacebookAccountRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWMergeFacebookAccountRequest

fun Single<NWMergeFacebookAccountRequest>.asDataEntity(): Single<DataMergeFacebookAccountRequest> {
    return this.map { it.asDataEntity() }
}


fun NWMergeFacebookAccountRequest.asDataEntity(): DataMergeFacebookAccountRequest {
    return DataMergeFacebookAccountRequest(
        facebook_uid,
        facebook_access_token
    )
}


fun Single<DataMergeFacebookAccountRequest>.asRemoteEntity(): Single<NWMergeFacebookAccountRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataMergeFacebookAccountRequest.asRemoteEntity() : NWMergeFacebookAccountRequest {
    return NWMergeFacebookAccountRequest(
        facebook_uid,
        facebook_access_token
    )
}
