package com.limor.app.mappers


import com.limor.app.uimodels.UIAuthResponse
import com.limor.app.uimodels.UIDataAuthResponse
import com.limor.app.uimodels.UIToken
import entities.response.AuthResponseEntity
import entities.response.DataAuthResponseEntity
import entities.response.DataTokenEntity
import io.reactivex.Single


fun Single<AuthResponseEntity>.asUIModel(): Single<UIAuthResponse> {
    return this.map { it.asUIModel() }
}

fun AuthResponseEntity.asUIModel() : UIAuthResponse {
    return UIAuthResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun DataAuthResponseEntity.asUIModel() : UIDataAuthResponse {
    return UIDataAuthResponse(
        token.asUIModel()
    )
}

fun DataTokenEntity.asUIModel(): UIToken {
    return UIToken(
        access_token,
        token_type,
        expires_in,
        scope,
        created_at
    )
}