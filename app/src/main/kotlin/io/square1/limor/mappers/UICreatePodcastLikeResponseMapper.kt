package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<CreatePodcastLikeResponseEntity>.asUIModel(): Single<UICreatePodcastLikeResponse> {
    return this.map { it.asUIModel() }
}


fun CreatePodcastLikeResponseEntity.asUIModel(): UICreatePodcastLikeResponse {
    return UICreatePodcastLikeResponse(
        code,
        message,
        data?.asUIModel()
    )
}


fun CreatePodcastLikeData.asUIModel(): UICreatePodcastLikeData {
    return UICreatePodcastLikeData(
        like?.asUIModel()
    )
}

fun PodcastLikeEntity.asUIModel(): UIPodcastLike {
    return UIPodcastLike(podcast_id, user_id)
}