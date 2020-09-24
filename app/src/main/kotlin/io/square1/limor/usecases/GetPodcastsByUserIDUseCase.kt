package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIGetPodcastsResponse
import repositories.user.UserRepository
import javax.inject.Inject

class GetPodcastsByUserIDUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int, limit: Int, offset: Int): Single<UIGetPodcastsResponse> {
        return userRepository.getPodcasts(id, limit, offset)
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}