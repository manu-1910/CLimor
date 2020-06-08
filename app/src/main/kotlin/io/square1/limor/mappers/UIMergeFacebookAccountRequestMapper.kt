package io.square1.limor.mappers

import entities.request.DataMergeFacebookAccountRequest
import io.reactivex.Single
import io.square1.limor.uimodels.UIMergeFacebookAccountRequest



fun UIMergeFacebookAccountRequest.asDataEntity(): DataMergeFacebookAccountRequest {
    return DataMergeFacebookAccountRequest(
        facebook_uid,
        facebook_access_token
    )
}


fun DataMergeFacebookAccountRequest.asUIModel(): UIMergeFacebookAccountRequest {
    return UIMergeFacebookAccountRequest(
        facebook_uid,
        facebook_access_token
    )
}


fun Single<DataMergeFacebookAccountRequest>.asUIModel(): Single<UIMergeFacebookAccountRequest> {
    return this.map { it.asUIModel() }
}

