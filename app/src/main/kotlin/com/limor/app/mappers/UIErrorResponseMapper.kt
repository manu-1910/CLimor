package com.limor.app.mappers


import com.limor.app.uimodels.UIErrorResponse
import entities.response.ErrorResponseEntity
import io.reactivex.Single


fun Single<ErrorResponseEntity>.asUIModel(): Single<UIErrorResponse> {
    return this.map { it.asUIModel() }
}


fun ErrorResponseEntity.asUIModel() : UIErrorResponse {
    return UIErrorResponse(
        code,
        message
    )
}


