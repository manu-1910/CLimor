package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIFeaturedPodcastsResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.podcast.PodcastRepository
import javax.inject.Inject


class FeaturedPodcastsUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(): Single<UIFeaturedPodcastsResponse> {
        return podcastRepository.getFeaturedPodcasts()
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}