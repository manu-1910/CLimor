package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CreateRecastUIModel
import com.limor.app.uimodels.DeleteRecastUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecastPodcastUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
)  {
    suspend fun executeRecast(castId: Int) : CreateRecastUIModel?{
        return withContext(dispatcherProvider.io) {
            repository.recastPodcast(castId)?.mapToUIModel()
        }
    }

    suspend fun executeDeleteRecast(castId: Int): DeleteRecastUIModel?{
        return withContext(dispatcherProvider.io){
            repository.deleteRecastPodcast(castId)?.mapToUIModel()
        }
    }
}