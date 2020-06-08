package io.square1.limor.mappers

import entities.request.DataTokenFBRequest
import io.reactivex.Single
import io.square1.limor.uimodels.UITokenFBRequest


fun UITokenFBRequest.asDataEntity(): DataTokenFBRequest {
    return DataTokenFBRequest(
        client_id,
        client_secret,
        grant_type,
        facebook_access_token,
        referral_code,
        user.asDataEntity()
    )
}


fun DataTokenFBRequest.asUIModel(): UITokenFBRequest {
    return UITokenFBRequest(
        client_id,
        client_secret,
        grant_type,
        facebook_access_token,
        referral_code,
        user.asUIModel()
    )
}



fun Single<DataTokenFBRequest>.asUIModel(): Single<UITokenFBRequest> {
    return this.map { it.asUIModel() }
}
