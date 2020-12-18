package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIGetBlockedUsersResponse
import com.limor.app.uimodels.UIGetFollowingsUsersResponse
import com.limor.app.uimodels.UIGetPodcastsResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.user.UserRepository
import javax.inject.Inject

class GetUserFollowingsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int, limit: Int, offset: Int): Single<UIGetFollowingsUsersResponse> {
        return userRepository.getFollowings(id, limit, offset)
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}