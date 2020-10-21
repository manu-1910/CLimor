package io.square1.limor.mappers

import entities.response.UpdatedData
import entities.response.UpdatedResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIUpdatedData
import io.square1.limor.uimodels.UIUpdatedResponse


fun Single<UpdatedResponseEntity>.asUIModel(): Single<UIUpdatedResponse> {
    return this.map { it.asUIModel() }
}


fun UpdatedResponseEntity.asUIModel(): UIUpdatedResponse {
    return UIUpdatedResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun UpdatedData.asUIModel(): UIUpdatedData {
    return UIUpdatedData(
        updated
    )
}
