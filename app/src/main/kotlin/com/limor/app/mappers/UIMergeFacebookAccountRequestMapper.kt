package com.limor.app.mappers

import com.limor.app.uimodels.UIMergeFacebookAccountRequest
import entities.request.DataMergeFacebookAccountRequest
import io.reactivex.Single


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

