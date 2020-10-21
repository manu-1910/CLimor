package com.limor.app.mappers

import entities.response.UpdatedData
import entities.response.UpdatedResponseEntity
import io.reactivex.Single
import com.limor.app.uimodels.UIUpdatedData
import com.limor.app.uimodels.UIUpdatedResponse


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
