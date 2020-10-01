package io.square1.limor.mappers

import entities.response.BlockedUserResponseEntity
import entities.response.UserBlockedEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIBlockedUserResponse
import io.square1.limor.uimodels.UIUserBlocked


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
