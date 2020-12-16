package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIGetPodcastsResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.categories.CategoriesRepository
import javax.inject.Inject

class GetPodcastsByCategoryUseCase @Inject constructor(
    private val categoriesRepository: CategoriesRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int, limit: Int?, offset: Int?): Single<UIGetPodcastsResponse> {
        return categoriesRepository.getPodcastByCategory(id, limit, offset)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}