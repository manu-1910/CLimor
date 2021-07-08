package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCommentsForPodcastUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(
        castId: Int,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): Result<List<CommentUIModel>> {
        return runCatching {
            withContext(dispatcherProvider.io) {
                repository.getCommentsByPodcast(castId, offset, limit).map {
                    it.mapToUIModel()
                }
            }
        }
    }
}