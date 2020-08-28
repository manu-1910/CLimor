package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<DeleteLikeResponseEntity>.asUIModel(): Single<UIDeleteLikeResponse> {
    return this.map { it.asUIModel() }
}


fun DeleteLikeResponseEntity.asUIModel(): UIDeleteLikeResponse {
    return UIDeleteLikeResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun DeleteLikeData.asUIModel(): UIDeleteLikeData {
    return UIDeleteLikeData(
        destroyed
    )
}
