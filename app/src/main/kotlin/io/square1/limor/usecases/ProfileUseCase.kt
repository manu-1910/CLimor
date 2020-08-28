package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asDataEntity
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UISignUpRequest
import io.square1.limor.uimodels.UISignUpResponse
import io.square1.limor.uimodels.UIUser
import repositories.auth.AuthRepository
import repositories.user.UserRepository

import javax.inject.Inject


class ProfileUseCase @Inject constructor(
    private val profileRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(): Single<UISignUpResponse> {
        return profileRepository.userMe()
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}