package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIGetBlockedUsersResponse
import repositories.user.UserRepository
import javax.inject.Inject

class GetBlockedUsersUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(limit: Int, offset: Int): Single<UIGetBlockedUsersResponse> {
        return userRepository.getBlockedUsers(limit, offset)
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}