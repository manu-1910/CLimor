package com.limor.app.mappers

import com.limor.app.uimodels.UIContentRequest
import entities.request.DataContentRequest
import io.reactivex.Single

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
