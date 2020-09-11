package io.square1.limor.usecases


import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UICategoriesResponse
import repositories.categories.CategoriesRepository
import javax.inject.Inject


class CategoriesUseCase @Inject constructor(
    private val categoriesRepository: CategoriesRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(): Single<UICategoriesResponse> {
        return categoriesRepository.getCategories()
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }

}