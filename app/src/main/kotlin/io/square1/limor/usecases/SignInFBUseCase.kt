package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asDataEntity
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIAuthResponse
import io.square1.limor.uimodels.UITokenFBRequest
import repositories.auth.AuthRepository
import javax.inject.Inject


class SignInFBUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiTokenFBRequest: UITokenFBRequest): Single<UIAuthResponse> {
        return authRepository.signInFB(uiTokenFBRequest.asDataEntity())
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}