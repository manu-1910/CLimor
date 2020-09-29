package io.square1.limor.mappers


import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<ChangePasswordResponseEntity>.asUIModel(): Single<UIChangePasswordResponse> {
    return this.map { it.asUIModel() }
}


fun ChangePasswordResponseEntity.asUIModel(): UIChangePasswordResponse{
    return UIChangePasswordResponse(
        code,
        message,
        data.asUIModel()
    )
}



