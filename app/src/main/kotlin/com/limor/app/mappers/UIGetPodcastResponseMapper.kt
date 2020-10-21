package com.limor.app.mappers

import com.limor.app.uimodels.UIGetPodcastItem
import com.limor.app.uimodels.UIGetPodcastResponse
import entities.response.GetPodcastResponseEntity
import entities.response.PodcastItemEntity
import io.reactivex.Single

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