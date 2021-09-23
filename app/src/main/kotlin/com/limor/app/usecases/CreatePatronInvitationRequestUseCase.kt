package com.limor.app.usecases

import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreatePatronInvitationRequestUseCase @Inject constructor(
    private val repository: com.limor.app.apollo.UserRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(userId: Int) {
        return withContext(dispatcherProvider.io) {
            repository.requestPatronInvitation(userId)
        }
    }
}