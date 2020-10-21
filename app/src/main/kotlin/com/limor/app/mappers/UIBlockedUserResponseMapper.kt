package com.limor.app.mappers

import com.limor.app.uimodels.UIBlockedUserResponse
import com.limor.app.uimodels.UIUserBlocked
import entities.response.BlockedUserResponseEntity
import entities.response.UserBlockedEntity
import io.reactivex.Single


fun Single<BlockedUserResponseEntity>.asUIModel(): Single<UIBlockedUserResponse> {
    return this.map { it.asUIModel() }
}


fun BlockedUserResponseEntity.asUIModel(): UIBlockedUserResponse {
    return UIBlockedUserResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun UserBlockedEntity.asUIModel(): UIUserBlocked {
    return UIUserBlocked(
        blocked
    )
}

fun UIBlockedUserResponse.asDataEntity(): BlockedUserResponseEntity {
    return BlockedUserResponseEntity(
        code, message, data.asDataEntity()
    )
}

fun UIUserBlocked.asDataEntity(): UserBlockedEntity {
    return UserBlockedEntity(
        blocked
    )
}
