package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.ShareCastUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SharePodcastUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(castId: Int, shareCount: Int = 1) : ShareCastUIModel?{
        return withContext(dispatcherProvider.io) {
            repository.sharePodcast(castId, shareCount)?.mapToUIModel()
        }
    }
}