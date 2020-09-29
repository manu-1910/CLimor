package io.square1.limor.usecases


import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asDataEntity
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIChangePasswordRequest
import io.square1.limor.uimodels.UIChangePasswordResponse
import repositories.auth.AuthRepository
import javax.inject.Inject


class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiChangePasswordRequest: UIChangePasswordRequest): Single<UIChangePasswordResponse> {
        return authRepository.changePassword(uiChangePasswordRequest.asDataEntity())
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}