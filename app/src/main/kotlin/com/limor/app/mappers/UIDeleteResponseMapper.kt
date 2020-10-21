package com.limor.app.mappers

import com.limor.app.uimodels.UIDeleteData
import com.limor.app.uimodels.UIDeleteResponse
import entities.response.DeleteData
import entities.response.DeleteResponseEntity
import io.reactivex.Single


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
