package com.limor.app.usecases


import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UICategoriesResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
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