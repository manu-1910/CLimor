package com.limor.app.mappers


import com.limor.app.uimodels.UIChangePasswordRequest
import entities.request.DataChangePasswordRequest
import io.reactivex.Single


fun UIChangePasswordRequest.asDataEntity(): DataChangePasswordRequest {
    return DataChangePasswordRequest(
        current_password, new_password
    )
}


fun Single<DataChangePasswordRequest>.asUIModel(): Single<UIChangePasswordRequest> {
    return this.map { it.asUIModel() }
}


fun DataChangePasswordRequest.asUIModel(): UIChangePasswordRequest {
    return UIChangePasswordRequest(
        current_password, new_password
    )
}

//
//fun Single<DataChangePasswordRequest>.asUIModel(): Single<UIChangePasswordRequest> {
//    return this.map { it.asUIModel() }
//}
