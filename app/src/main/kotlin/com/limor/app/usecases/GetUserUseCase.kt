package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIGetUserResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.user.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val usersRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int?): Single<UIGetUserResponse> {
        id?.let {
            return usersRepository.getUser(it)
                .asUIModel()
                .observeOn(postExecutionThread.getScheduler())
                ?.subscribeOn(jobExecutor.getScheduler())!!
        } ?: run {
            return usersRepository.userMe()
                .asUIModel()
                .observeOn(postExecutionThread.getScheduler())
                ?.subscribeOn(jobExecutor.getScheduler())!!
        }
    }
}