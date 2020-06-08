package io.square1.limor.scenes.authentication.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function3
import io.square1.limor.common.SessionManager
import io.square1.limor.common.SingleLiveEvent
import io.square1.limor.uimodels.UIErrorResponse
import io.square1.limor.usecases.SignInUseCase
import io.square1.limor.extensions.trackProgress
import io.square1.limor.extensions.trackErrorResponse

import javax.inject.Inject



class SignViewModel @Inject constructor(private val singInUseCase: SignInUseCase, private val sessionManager: SessionManager) : ViewModel() {
    var emailSavedViewModel: String = ""

   data class Input(
       val emailObservable: Observable<String>,
       val passwordObservable: Observable<String>,
       val singInTrigger: Observable<Unit>
   )

   data class Output(
       val response: LiveData<Boolean>,
       val backgroundWorkingProgress: LiveData<Boolean>,
       val errorMessage: SingleLiveEvent<UIErrorResponse>
   )

   fun transform(input: Input): Output {
       val errorTracker = SingleLiveEvent<UIErrorResponse>()
       val backgroundWorkingProgress = MutableLiveData<Boolean>()

       val singInResponse = LiveDataReactiveStreams.fromPublisher(
           formObservable(input, errorTracker)
               .flatMapSingle { doLogin(it, errorTracker, backgroundWorkingProgress) }
               .toFlowable(BackpressureStrategy.LATEST)
               .onErrorReturn { false }
       )
       return Output(singInResponse, backgroundWorkingProgress, errorTracker)
   }


   private fun formObservable(
       input: Input,
       errorTracker: SingleLiveEvent<UIErrorResponse>
   ): Observable<Pair<String, String>> = input.singInTrigger.withLatestFrom(
       input.emailObservable,
       input.passwordObservable,
       Function3<Unit, String, String, Pair<String, String>> { _, email: String, password: String ->
           Pair(email, password)
       }).trackErrorResponse(errorTracker)


   private fun doLogin(
       it: Pair<String, String>,
       errorTracker: SingleLiveEvent<UIErrorResponse>,
       backgroundWorkingProgress: MutableLiveData<Boolean>
   ): Single<Boolean>? = singInUseCase.execute(it.first, it.second)
       .map {
           sessionManager.storeToken(it.data.token.access_token)
       }
       .trackProgress(backgroundWorkingProgress)
       .trackErrorResponse(errorTracker)
       .onErrorReturn { false }

}


