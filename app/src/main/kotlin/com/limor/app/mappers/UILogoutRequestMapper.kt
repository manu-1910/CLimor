package com.limor.app.mappers


import com.limor.app.uimodels.UILogoutRequest
import entities.request.DataLogoutRequest
import io.reactivex.Single


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
