package io.square1.limor.mappers


import entities.request.DataSignUpRequest
import entities.request.DataSignUpUser
import io.reactivex.Single
import io.square1.limor.uimodels.UISignUpRequest
import io.square1.limor.uimodels.UISignUpUser


fun UISignUpRequest.asDataEntity(): DataSignUpRequest {
    return DataSignUpRequest(
        client_id,
        client_secret,
        scopes,
        user.asDataEntity()
    )
}


fun UISignUpUser.asDataEntity(): DataSignUpUser {
    return DataSignUpUser(
        email,
        password,
        username
    )
}


fun DataSignUpRequest.asUIModel(): UISignUpRequest {
    return UISignUpRequest(
        client_id,
        client_secret,
        scopes,
        user.asUIModel()
    )
}

fun DataSignUpUser.asUIModel(): UISignUpUser {
    return UISignUpUser(
        email,
        password,
        username
    )
}

fun Single<DataSignUpRequest>.asUIModel(): Single<UISignUpRequest> {
    return this.map { it.asUIModel() }
}
