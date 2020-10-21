package com.limor.app.mappers

import com.limor.app.uimodels.UITokenFBRequest
import entities.request.DataTokenFBRequest
import io.reactivex.Single


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
