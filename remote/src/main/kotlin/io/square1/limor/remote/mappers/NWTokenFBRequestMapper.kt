package io.square1.limor.remote.mappers

import entities.request.DataTokenFBRequest
import io.square1.limor.remote.entities.requests.NWTokenFBRequest
import io.reactivex.Single


fun Single<NWTokenFBRequest>.asDataEntity(): Single<DataTokenFBRequest> {
    return this.map { it.asDataEntity() }
}


fun NWTokenFBRequest.asDataEntity(): DataTokenFBRequest {
    return DataTokenFBRequest(
        client_id,
        client_secret,
        grant_type,
        facebook_access_token,
        referral_code,
        user.asDataEntity()
    )
}


fun Single<DataTokenFBRequest>.asRemoteEntity(): Single<NWTokenFBRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataTokenFBRequest.asRemoteEntity() : NWTokenFBRequest {
    return NWTokenFBRequest(
        client_id,
        client_secret,
        grant_type,
        facebook_access_token,
        referral_code,
        user.asRemoteEntity()
    )
}
