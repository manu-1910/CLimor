package com.limor.app.mappers

import com.limor.app.uimodels.UICreatePodcastRecastData
import com.limor.app.uimodels.UICreatePodcastRecastResponse
import com.limor.app.uimodels.UIPodcastRecast
import entities.response.CreatePodcastRecastData
import entities.response.CreatePodcastRecastResponseEntity
import entities.response.PodcastRecastEntity
import io.reactivex.Single


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