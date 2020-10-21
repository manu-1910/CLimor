package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import io.reactivex.Completable
import io.square1.limor.remote.executors.JobExecutor
import repositories.auth.AuthRepository
import javax.inject.Inject


//class ForgotPasswordUseCase @Inject constructor(
//    private val authRepository: AuthRepository,
//    private val postExecutionThread: PostExecutionThread,
//    private val jobExecutor: JobExecutor
//) {
//    fun execute(forgotPasswordRequest: UIForgotPasswordRequest): Completable {
//        return authRepository.forgotPass(forgotPasswordRequest.email)
//            .observeOn(postExecutionThread.getScheduler())
//            .subscribeOn(jobExecutor.getScheduler())
//    }
//}

class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(email: String): Completable {
        return authRepository.forgotPass(email)
            .observeOn(postExecutionThread.getScheduler())
            .subscribeOn(jobExecutor.getScheduler())
    }
}