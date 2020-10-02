package io.square1.limor.remote.mappers

import entities.request.DataSignUpFacebookRequest
import entities.request.DataSignUpFacebookUser
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWSignUpFacebookRequest
import io.square1.limor.remote.entities.requests.NWSignUpFacebookUser


fun Single<NWSignUpFacebookRequest>.asDataEntity(): Single<DataSignUpFacebookRequest> {
    return this.map { it.asDataEntity() }
}

fun NWSignUpFacebookRequest.asDataEntity(): DataSignUpFacebookRequest {
    return DataSignUpFacebookRequest(
        client_id,
        client_secret,
        scopes,
        user.asDataEntity()
    )
}


fun NWSignUpFacebookUser.asDataEntity(): DataSignUpFacebookUser {
    return DataSignUpFacebookUser(
        facebook_uid,
        facebook_token,
        email,
        password,
        username
    )
}


fun Single<DataSignUpFacebookRequest>.asRemoteEntity(): Single<NWSignUpFacebookRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataSignUpFacebookRequest.asRemoteEntity() : NWSignUpFacebookRequest {
    return NWSignUpFacebookRequest(
        client_id,
        client_secret,
        scopes,
        user.asRemoteEntity()
    )
}

fun DataSignUpFacebookUser.asRemoteEntity() : NWSignUpFacebookUser {
    return NWSignUpFacebookUser(
        facebook_uid,
        facebook_token,
        email,
        password,
        username
    )
}