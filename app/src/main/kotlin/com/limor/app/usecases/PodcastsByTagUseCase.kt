package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIGetPodcastsResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.search.SearchRepository
import javax.inject.Inject

class PodcastsByTagUseCase @Inject constructor(
    private val searchRepository: SearchRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(limit: Int, offset: Int, tag: String): Single<UIGetPodcastsResponse> {
        return searchRepository.podcastsByTag(limit, offset, tag)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }

}