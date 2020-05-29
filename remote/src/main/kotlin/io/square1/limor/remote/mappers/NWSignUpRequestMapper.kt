package io.square1.limor.remote.mappers

import entities.request.DataSignUpRequest
import entities.request.DataSignUpUser
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWSignUpRequest
import io.square1.limor.remote.entities.requests.NWSignUpUser


fun Single<NWSignUpRequest>.asDataEntity(): Single<DataSignUpRequest> {
    return this.map { it.asDataEntity() }
}

fun NWSignUpRequest.asDataEntity(): DataSignUpRequest {
    return DataSignUpRequest(
        client_id,
        client_secret,
        scopes,
        user.asDataEntity()
    )
}


fun NWSignUpUser.asDataEntity(): DataSignUpUser {
    return DataSignUpUser(
        email,
        password,
        username
    )
}


fun Single<DataSignUpRequest>.asRemoteEntity(): Single<NWSignUpRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataSignUpRequest.asRemoteEntity() : NWSignUpRequest {
    return NWSignUpRequest(
        client_id,
        client_secret,
        scopes,
        user.asRemoteEntity()
    )
}

fun DataSignUpUser.asRemoteEntity() : NWSignUpUser {
    return NWSignUpUser(
        email,
        password,
        username
    )
}