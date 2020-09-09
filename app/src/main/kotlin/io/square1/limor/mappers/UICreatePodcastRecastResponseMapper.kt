package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<CreatePodcastRecastResponseEntity>.asUIModel(): Single<UICreatePodcastRecastResponse> {
    return this.map { it.asUIModel() }
}


fun CreatePodcastRecastResponseEntity.asUIModel(): UICreatePodcastRecastResponse {
    return UICreatePodcastRecastResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun CreatePodcastRecastData.asUIModel(): UICreatePodcastRecastData {
    return UICreatePodcastRecastData(
        recast?.asUIModel()
    )
}

fun PodcastRecastEntity.asUIModel(): UIPodcastRecast {
    return UIPodcastRecast(podcast_id, user_id)
}