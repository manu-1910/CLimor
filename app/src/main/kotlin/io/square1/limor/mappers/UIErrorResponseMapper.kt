package io.square1.limor.mappers


import entities.response.ErrorResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIErrorResponse



fun Single<ErrorResponseEntity>.asUIModel(): Single<UIErrorResponse> {
    return this.map { it.asUIModel() }
}


fun ErrorResponseEntity.asUIModel() : UIErrorResponse{
    return UIErrorResponse(
        code,
        message
    )
}


