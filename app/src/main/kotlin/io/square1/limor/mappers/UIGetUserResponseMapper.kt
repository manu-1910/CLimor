package io.square1.limor.mappers

import entities.response.GetUserEntity
import entities.response.GetUserResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIGetUserData
import io.square1.limor.uimodels.UIGetUserResponse


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
