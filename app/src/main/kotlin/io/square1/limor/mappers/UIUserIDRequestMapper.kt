package io.square1.limor.mappers

import entities.request.DataUserIDRequest
import io.reactivex.Single
import io.square1.limor.uimodels.UIUserIDRequest


fun Single<DataUserIDRequest>.asUIModel(): Single<UIUserIDRequest> {
    return this.map { it.asUIModel() }
}


fun DataUserIDRequest.asUIModel(): UIUserIDRequest {
    return UIUserIDRequest(
        user_id
    )
}

fun UIUserIDRequest.asDataEntity(): DataUserIDRequest {
    return DataUserIDRequest(
        user_id
    )
}
