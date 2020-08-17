package io.square1.limor.remote.mappers

import entities.request.DataCreateFriendRequest
import entities.request.DataForgotPasswordRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateFriendRequest
import io.square1.limor.remote.entities.requests.NWForgotPasswordRequest


fun Single<NWCreateFriendRequest>.asDataEntity(): Single<DataCreateFriendRequest> {
    return this.map { it.asDataEntity() }
}


fun NWCreateFriendRequest.asDataEntity(): DataCreateFriendRequest {
    return DataCreateFriendRequest(
        user_id
    )
}


fun DataCreateFriendRequest.asRemoteEntity() : NWCreateFriendRequest {
    return NWCreateFriendRequest(
        user_id
    )
}

