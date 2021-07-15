package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCommentByIdUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(
        commentId: Int
    ): Result<CommentUIModel?> {
        return runCatching {
            withContext(dispatcherProvider.io) {
                repository.getCommentById(commentId)?.mapToUIModel()
            }
        }
    }
}