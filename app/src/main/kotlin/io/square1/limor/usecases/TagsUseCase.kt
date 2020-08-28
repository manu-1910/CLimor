package io.square1.limor.usecases


import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asDataEntity
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIPromotedTagsResponse
import io.square1.limor.uimodels.UITagsRequest
import io.square1.limor.uimodels.UITagsResponse
import repositories.search.SearchRepository
import javax.inject.Inject


class TagsUseCase @Inject constructor(
    private val searchRepository: SearchRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiTagsRequest: UITagsRequest): Single<UITagsResponse> {
        return searchRepository.searchTag(uiTagsRequest.asDataEntity())
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }

//    fun execute(uiTagsRequest: UITagsRequest): Single<UITagsResponse> {
//        return searchRepository.trendingTags()
//            ?.asUIModel()
//            ?.observeOn(postExecutionThread.getScheduler())
//            ?.subscribeOn(jobExecutor.getScheduler())!!
//    }


//    fun execute(uiTagsRequest: UITagsRequest): Single<UIPromotedTagsResponse> {
//        return searchRepository.promotedTags()
//            ?.asUIModel()
//            ?.observeOn(postExecutionThread.getScheduler())
//            ?.subscribeOn(jobExecutor.getScheduler())!!
//    }
}