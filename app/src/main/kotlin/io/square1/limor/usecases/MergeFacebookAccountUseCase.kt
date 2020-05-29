package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asDataEntity
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIAuthResponse
import io.square1.limor.uimodels.UIMergeFacebookAccountRequest
import repositories.auth.AuthRepository
import javax.inject.Inject


class MergeFacebookAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiMergeFacebookAccountRequest: UIMergeFacebookAccountRequest): Single<UIAuthResponse> {
        return authRepository.mergeFacebookAccount(uiMergeFacebookAccountRequest.asDataEntity())
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}