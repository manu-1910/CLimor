package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UITagsResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.search.SearchRepository
import javax.inject.Inject

class TrendingTagsUseCase @Inject constructor(
    private val tagsRepository: SearchRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(): Single<UITagsResponse> {
        return tagsRepository.trendingTags()
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }

}