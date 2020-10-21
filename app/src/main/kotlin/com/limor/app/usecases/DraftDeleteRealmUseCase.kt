package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asDataEntity
import com.limor.app.uimodels.UIDraft
import io.reactivex.Completable
import io.square1.limor.remote.executors.JobExecutor
import repositories.drafts.DataDraftsRepository
import javax.inject.Inject

class DraftDeleteRealmUseCase @Inject constructor(
    private val dataDraftsRepository: DataDraftsRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiDraft: UIDraft): Completable {
        return dataDraftsRepository.deleteRealmDraft(uiDraft.asDataEntity())
            .subscribeOn(jobExecutor.getScheduler())
            .observeOn(postExecutionThread.getScheduler())
    }
}