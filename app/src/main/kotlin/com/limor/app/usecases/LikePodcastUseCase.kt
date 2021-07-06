package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CastUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LikePodcastUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(cast: CastUIModel, like: Boolean) {
        return withContext(dispatcherProvider.io) {
            if (like) {
                repository.likePodcast(cast.id)
            } else {
                repository.unLikePodcast(cast.id)
            }
        }
    }
}