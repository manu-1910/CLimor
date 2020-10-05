package io.square1.limor.mappers

import entities.request.DataContentRequest
import io.reactivex.Single
import io.square1.limor.uimodels.UIContentRequest

fun UIContentRequest.asDataEntity(): DataContentRequest {
    return DataContentRequest(
        content
    )
}


fun Single<DataContentRequest>.asUIModel(): Single<UIContentRequest> {
    return this.map { it.asUIModel() }
}


fun DataContentRequest.asUIModel(): UIContentRequest {
    return UIContentRequest(
        content
    )
}
