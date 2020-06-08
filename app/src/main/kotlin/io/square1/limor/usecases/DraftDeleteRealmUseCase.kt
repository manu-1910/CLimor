package io.square1.limor.usecases

import io.reactivex.Completable
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asDataEntity
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIDraft
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