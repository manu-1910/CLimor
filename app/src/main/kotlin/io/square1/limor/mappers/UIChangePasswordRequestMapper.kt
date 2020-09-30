package io.square1.limor.mappers


import entities.request.DataChangePasswordRequest
import io.reactivex.Single
import io.square1.limor.uimodels.UIChangePasswordRequest


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
