package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UICreateDeleteFriendResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.user.UserRepository
import javax.inject.Inject

class CreateFriendUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id : Int): Single<UICreateDeleteFriendResponse> {
        return userRepository.createFriend(id)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}