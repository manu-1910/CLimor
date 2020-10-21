package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asDataEntity
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIBlockedUserResponse
import com.limor.app.uimodels.UIUserIDRequest
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.user.UserRepository
import javax.inject.Inject

class DeleteBlockedUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(request: UIUserIDRequest): Single<UIBlockedUserResponse> {
        return userRepository.deleteUserBlocked(request.asDataEntity())
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}