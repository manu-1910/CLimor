package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ListenPodcastUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatchProvider: DispatcherProvider
){
    suspend fun execute(castId: Int) : Boolean?{
        return withContext(dispatchProvider.io){
            repository.listenPodcast(castId)
        }
    }
}