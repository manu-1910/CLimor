package com.limor.app.mappers

import entities.request.DataDropOffRequest
import io.reactivex.Single
import com.limor.app.uimodels.UIDropOffRequest


fun Single<DataDropOffRequest>.asUIModel(): Single<UIDropOffRequest> {
    return this.map { it.asUIModel() }
}


fun DataDropOffRequest.asUIModel(): UIDropOffRequest {
    return UIDropOffRequest(
        percentage
    )
}

fun UIDropOffRequest.asDataEntity(): DataDropOffRequest {
    return DataDropOffRequest(
        percentage
    )
}