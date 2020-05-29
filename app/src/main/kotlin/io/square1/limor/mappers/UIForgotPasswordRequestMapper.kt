package io.square1.limor.mappers

import entities.request.DataForgotPasswordRequest
import io.reactivex.Single
import io.square1.limor.uimodels.UIForgotPasswordRequest


fun UIForgotPasswordRequest.asUIModel(): DataForgotPasswordRequest {
    return DataForgotPasswordRequest(
        email
    )
}


fun Single<DataForgotPasswordRequest>.asUIModel(): Single<UIForgotPasswordRequest> {
    return this.map { it.asUIModel() }
}


fun DataForgotPasswordRequest.asUIModel() : UIForgotPasswordRequest{
    return UIForgotPasswordRequest(
        email
    )
}


