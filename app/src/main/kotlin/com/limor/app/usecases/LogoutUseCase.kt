package com.limor.app.usecases


import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asDataEntity
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UILogoutRequest
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
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
