package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIPodcastsByTagResponse
import repositories.search.SearchRepository
import javax.inject.Inject

class PodcastsByTagUseCase @Inject constructor(
    private val searchRepository: SearchRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(limit: Int, offset: Int, tag: String): Single<UIPodcastsByTagResponse> {
        return searchRepository.podcastsByTag(limit, offset, tag)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }

}