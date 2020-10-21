package com.limor.app.usecases


import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asDataEntity
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UISignUpFacebookRequest
import com.limor.app.uimodels.UISignUpResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.auth.AuthRepository
import javax.inject.Inject


class SignUpFBUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(uiSignUpFacebookRequest: UISignUpFacebookRequest): Single<UISignUpResponse> {
        return authRepository.signUpFB(uiSignUpFacebookRequest.asDataEntity())
            .asUIModel()
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}