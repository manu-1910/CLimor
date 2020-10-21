package com.limor.app.usecases


import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asDataEntity
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIPublishRequest
import com.limor.app.uimodels.UIPublishResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.podcast.PodcastRepository
import javax.inject.Inject


class PublishUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiPublishRequest: UIPublishRequest): Single<UIPublishResponse> {
        return podcastRepository.publishPodcast(uiPublishRequest.asDataEntity())
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}