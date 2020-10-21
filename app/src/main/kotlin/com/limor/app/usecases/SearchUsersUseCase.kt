package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UISuggestedUsersResponse
import entities.request.DataSearchTermRequest
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.search.SearchRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val searchRepository: SearchRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(term: String): Single<UISuggestedUsersResponse> {
        return searchRepository.searchUsers(DataSearchTermRequest(term))
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}