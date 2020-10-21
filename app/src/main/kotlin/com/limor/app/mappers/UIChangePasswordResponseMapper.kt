package com.limor.app.mappers


import com.limor.app.uimodels.UIChangePasswordResponse
import entities.response.ChangePasswordResponseEntity
import io.reactivex.Single


fun Single<ChangePasswordResponseEntity>.asUIModel(): Single<UIChangePasswordResponse> {
    return this.map { it.asUIModel() }
}


fun ChangePasswordResponseEntity.asUIModel(): UIChangePasswordResponse {
    return UIChangePasswordResponse(
        code,
        message,
        data.asUIModel()
    )
}



