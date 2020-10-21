package com.limor.app.mappers

import com.limor.app.uimodels.UICreatePodcastLikeData
import com.limor.app.uimodels.UICreatePodcastLikeResponse
import com.limor.app.uimodels.UIPodcastLike
import entities.response.CreatePodcastLikeData
import entities.response.CreatePodcastLikeResponseEntity
import entities.response.PodcastLikeEntity
import io.reactivex.Single


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