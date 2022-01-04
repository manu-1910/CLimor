package com.limor.app.usecases

import com.limor.app.apollo.PublishRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PatronPodcastUseCase @Inject constructor(
    private val repository: PublishRepository,
    private val dispatcherProvider: DispatcherProvider
){
    suspend fun executeAllCastsPriceUpdate(priceId: String): Result<String> = runCatching{
        withContext(dispatcherProvider.io){
            repository.updatePriceForAllCasts(priceId)
        }
    }
    suspend fun executeSingleCastPriceUpdate(castId: Int, priceId: String): Result<String> = runCatching{
        withContext(dispatcherProvider.io){
            repository.updatePriceForCast(castId, priceId)
        }
    }

    suspend fun executeInviteInternalUser(userId: Int): Result<String> = runCatching {
        withContext(dispatcherProvider.io){
            repository.inviteInternalUser(userId)
        }
    }

    suspend fun executeInviteExternal(numbers : List<String>): Result<String> = runCatching {
        withContext(dispatcherProvider.io){
            repository.inviteExternal(numbers)
        }
    }
}