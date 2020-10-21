package com.limor.app.usecases


import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asDataEntity
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UILocationsRequest
import com.limor.app.uimodels.UILocationsResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.search.SearchRepository
import javax.inject.Inject


class LocationsUseCase @Inject constructor(
    private val searchRepository: SearchRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiLocationsRequest: UILocationsRequest): Single<UILocationsResponse> {
        return searchRepository.searchLocations(uiLocationsRequest.asDataEntity())
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}