package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asDataEntity
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIGetUserResponse
import com.limor.app.uimodels.UIUpdateProfileRequest
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.user.UserRepository
import javax.inject.Inject


class ProfileUseCase @Inject constructor(
    private val profileRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(): Single<UIGetUserResponse> {
        return profileRepository.userMe()
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }

    fun executeUpdate(uiUpdateProfileRequest: UIUpdateProfileRequest): Single<UIGetUserResponse> {
        return profileRepository.userMeUpdate(uiUpdateProfileRequest.asDataEntity())
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}