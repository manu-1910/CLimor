package io.square1.limor.usecases

import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIDraft
import repositories.drafts.DataDraftsRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.Flowable
import javax.inject.Inject

class DraftLoadRealmUseCase @Inject constructor(
    private val dataDraftsRepository: DataDraftsRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(): LiveData<List<UIDraft>> {
        val flowable: Flowable<List<UIDraft>> =
            dataDraftsRepository.loadRealmDrafts()
                .map { uiDraftList -> uiDraftList.map { it.asUIModel() } }
                .observeOn(postExecutionThread.getScheduler())
        return LiveDataReactiveStreams.fromPublisher(flowable)
    }
}

