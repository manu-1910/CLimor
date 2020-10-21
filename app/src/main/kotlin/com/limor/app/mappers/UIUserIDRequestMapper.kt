package com.limor.app.mappers

import com.limor.app.uimodels.UIUserIDRequest
import entities.request.DataUserIDRequest
import io.reactivex.Single


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
