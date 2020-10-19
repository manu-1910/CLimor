package io.square1.limor.usecases


import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asDataEntity
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UIUserDeviceRequest
import io.square1.limor.uimodels.UIUserDeviceResponse
import repositories.user.UserRepository
import javax.inject.Inject

class NotificationsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiUserDeviceRequest: UIUserDeviceRequest): Single<UIUserDeviceResponse> {
        return userRepository.sendUserDevice(uiUserDeviceRequest.asDataEntity())
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}