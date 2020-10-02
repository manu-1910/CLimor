package io.square1.limor.mappers


import entities.request.DataSignUpFacebookRequest
import entities.request.DataSignUpFacebookUser
import io.reactivex.Single
import io.square1.limor.uimodels.UISignUpFacebookRequest
import io.square1.limor.uimodels.UISignUpFacebookUser


fun UISignUpFacebookRequest.asDataEntity(): DataSignUpFacebookRequest {
    return DataSignUpFacebookRequest(
        client_id,
        client_secret,
        scopes,
        user.asDataEntity()
    )
}


fun UISignUpFacebookUser.asDataEntity(): DataSignUpFacebookUser {
    return DataSignUpFacebookUser(
        facebook_uid,
        facebook_token,
        email,
        password,
        username
    )
}


fun DataSignUpFacebookRequest.asUIModel(): UISignUpFacebookRequest {
    return UISignUpFacebookRequest(
        client_id,
        client_secret,
        scopes,
        user.asUIModel()
    )
}

fun DataSignUpFacebookUser.asUIModel(): UISignUpFacebookUser {
    return UISignUpFacebookUser(
        facebook_uid,
        facebook_token,
        email,
        password,
        username
    )
}

fun Single<DataSignUpFacebookRequest>.asUIModel(): Single<UISignUpFacebookRequest> {
    return this.map { it.asUIModel() }
}
