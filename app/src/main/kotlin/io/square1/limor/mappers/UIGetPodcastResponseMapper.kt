package io.square1.limor.mappers

import entities.response.GetPodcastResponseEntity
import entities.response.PodcastItemEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIGetPodcastItem
import io.square1.limor.uimodels.UIGetPodcastResponse

fun Single<GetPodcastResponseEntity>.asUIModel(): Single<UIGetPodcastResponse> {
    return this.map { it.asUIModel() }
}

fun GetPodcastResponseEntity.asUIModel(): UIGetPodcastResponse {
    return UIGetPodcastResponse(
        code,
        message,
        data.asUIModel()
    )
}


fun PodcastItemEntity.asUIModel(): UIGetPodcastItem {
    return UIGetPodcastItem(
        podcast.asUIModel()
    )
}