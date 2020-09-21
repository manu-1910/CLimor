package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIGetUserResponse
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