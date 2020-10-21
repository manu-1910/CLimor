package com.limor.app.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIDraft
import io.reactivex.Flowable
import io.square1.limor.remote.executors.JobExecutor
import repositories.drafts.DataDraftsRepository
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

