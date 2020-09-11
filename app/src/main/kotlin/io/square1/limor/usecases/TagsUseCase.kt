package io.square1.limor.usecases


import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UITagsResponse
import repositories.search.SearchRepository
import javax.inject.Inject


class TagsUseCase @Inject constructor(
    private val tagsRepository: SearchRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(tag: String): Single<UITagsResponse> {
        return tagsRepository.searchTag(tag)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }

}