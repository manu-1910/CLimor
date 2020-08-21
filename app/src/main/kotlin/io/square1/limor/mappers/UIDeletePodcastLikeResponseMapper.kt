package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<DeletePodcastLikeResponseEntity>.asUIModel(): Single<UIDeletePodcastLikeResponse> {
    return this.map { it.asUIModel() }
}


fun DeletePodcastLikeResponseEntity.asUIModel(): UIDeletePodcastLikeResponse {
    return UIDeletePodcastLikeResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun DeletePodcastLikeData.asUIModel(): UIDeletePodcastLikeData {
    return UIDeletePodcastLikeData(
        destroyed
    )
}
