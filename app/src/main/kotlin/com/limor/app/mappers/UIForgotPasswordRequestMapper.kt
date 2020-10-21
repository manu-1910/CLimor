package com.limor.app.mappers

import com.limor.app.uimodels.UIForgotPasswordRequest
import entities.request.DataForgotPasswordRequest
import io.reactivex.Single


fun UIForgotPasswordRequest.asUIModel(): DataForgotPasswordRequest {
    return DataForgotPasswordRequest(
        email
    )
}


fun Single<DataForgotPasswordRequest>.asUIModel(): Single<UIForgotPasswordRequest> {
    return this.map { it.asUIModel() }
}


fun DataForgotPasswordRequest.asUIModel() : UIForgotPasswordRequest {
    return UIForgotPasswordRequest(
        email
    )
}


