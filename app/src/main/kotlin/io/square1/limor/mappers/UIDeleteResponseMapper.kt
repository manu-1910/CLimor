package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<DeleteResponseEntity>.asUIModel(): Single<UIDeleteResponse> {
    return this.map { it.asUIModel() }
}


fun DeleteResponseEntity.asUIModel(): UIDeleteResponse {
    return UIDeleteResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun DeleteData.asUIModel(): UIDeleteData {
    return UIDeleteData(
        destroyed
    )
}
