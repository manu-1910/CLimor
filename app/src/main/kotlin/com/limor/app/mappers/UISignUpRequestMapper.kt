package com.limor.app.mappers


import com.limor.app.uimodels.UISignUpRequest
import com.limor.app.uimodels.UISignUpUser
import entities.request.DataSignUpRequest
import entities.request.DataSignUpUser
import io.reactivex.Single


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
