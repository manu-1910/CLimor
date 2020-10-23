package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIGetNotificationsResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.user.UserRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(limit: Int, offset: Int): Single<UIGetNotificationsResponse> {
        return userRepository.getNotifications(limit, offset)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }

}