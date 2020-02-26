package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.remote.executors.JobExecutor
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    //private val authRepository: AuthRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
   // fun execute(email: String, password: String): Single<UIAuthResponse>{
   //     return authRepository.signIn(email,password)
   //         .asUIModel()
   //         .observeOn(postExecutionThread.getScheduler())
   //         .subscribeOn(jobExecutor.getScheduler())
   // }
}