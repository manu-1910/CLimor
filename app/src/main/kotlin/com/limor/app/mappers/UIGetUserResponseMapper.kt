package com.limor.app.mappers

import com.limor.app.uimodels.UIGetUserData
import com.limor.app.uimodels.UIGetUserResponse
import entities.response.GetUserEntity
import entities.response.GetUserResponseEntity
import io.reactivex.Single


fun Single<GetUserResponseEntity>.asUIModel(): Single<UIGetUserResponse> {
    return this.map { it.asUIModel() }
}


fun GetUserResponseEntity.asUIModel(): UIGetUserResponse {
    return UIGetUserResponse(
        code,
        message,
        data.asUIModel()
    )
}


fun GetUserEntity.asUIModel(): UIGetUserData {
    return UIGetUserData(user.asUIModel())
}
