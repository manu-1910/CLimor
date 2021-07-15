package com.limor.app.usecases

import com.limor.app.CreateRecastMutation
import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecastPodcastUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
)  {
    suspend fun execute(castId: Int) : CreateRecastMutation.CreateRecast?{
        return withContext(dispatcherProvider.io) {
            repository.recastPodcast(castId)
        }
    }
}