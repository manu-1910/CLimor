package io.square1.limor.usecases


import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asDataEntity
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIErrorResponse
import io.square1.limor.uimodels.UILogoutRequest
import io.square1.limor.uimodels.UISignUpResponse
import repositories.user.UserRepository

import javax.inject.Inject


class LogoutUseCase @Inject constructor(
    private val profileRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiLogoutRequest: UILogoutRequest): Single<UIErrorResponse> {
        return profileRepository.logOut(uiLogoutRequest.asDataEntity())
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}
