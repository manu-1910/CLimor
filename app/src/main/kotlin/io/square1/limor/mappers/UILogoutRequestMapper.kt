package io.square1.limor.mappers


import entities.request.DataLogoutRequest
import io.reactivex.Single
import io.square1.limor.uimodels.UILogoutRequest


fun UILogoutRequest.asDataEntity(): DataLogoutRequest {
    return DataLogoutRequest(
        token,
        uuid
    )
}


fun DataLogoutRequest.asUIModel(): UILogoutRequest {
    return UILogoutRequest(
        token,
        uuid
    )
}


fun Single<DataLogoutRequest>.asUIModel(): Single<UILogoutRequest> {
    return this.map { it.asUIModel() }
}
