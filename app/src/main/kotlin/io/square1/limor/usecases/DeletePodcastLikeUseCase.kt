package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UICreatePodcastLikeResponse
import io.square1.limor.uimodels.UIDeletePodcastLikeResponse
import repositories.podcast.PodcastRepository
import javax.inject.Inject


class DeletePodcastLikeUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id : Int): Single<UIDeletePodcastLikeResponse> {
        return podcastRepository.dislikePodcast(id)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}